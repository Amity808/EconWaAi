package org.ecom.econwaai.compliance.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.common.exception.ErrorCode;
import org.ecom.econwaai.web.dto.WebviewUrlResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebviewTokenService {

    private static final Logger log = LoggerFactory.getLogger(WebviewTokenService.class);
    public static final int WEBVIEW_TOKEN_TTL_SECONDS = 900; // 15 minutes

    private final SecretKey signingKey;
    private final String baseUrl;
    // Set of consumed tokens to ensure single-use
    private final Set<String> consumedTokens = ConcurrentHashMap.newKeySet();

    public WebviewTokenService(
            @Value("${econwa.jwt.secret:EconWaSecretKeyMustBeAtLeast32BytesLongForSecurityStandard2026}") String secret,
            @Value("${econwa.kyc.webview-base-url:https://app.econwa.ng/kyc/webview}") String baseUrl
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.baseUrl = baseUrl;
    }

    /**
     * Generates a signed, single-use, 15-minute expiring webview URL for WhatsApp KYC handoff.
     */
    public WebviewUrlResponseDto generateWebviewUrl(UUID userId, UUID orgId, String flowType, String returnUrl) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(WEBVIEW_TOKEN_TTL_SECONDS);
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .id(jti)
                .subject(userId.toString())
                .claim("org_id", orgId.toString())
                .claim("flow_type", flowType)
                .claim("return_url", returnUrl != null ? returnUrl : "")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();

        String webviewUrl = baseUrl + "?token=" + token;
        log.info("Generated single-use webview KYC URL for user={} org={} flow={} jti={}",
                userId, orgId, flowType, jti);

        return new WebviewUrlResponseDto(webviewUrl, token, expiresAt, WEBVIEW_TOKEN_TTL_SECONDS);
    }

    /**
     * Validates and single-use consumes the webview token.
     * Prevents replay attacks.
     */
    public Claims validateAndConsumeToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String jti = claims.getId();
            if (jti == null || !consumedTokens.add(jti)) {
                log.warn("Attempted replay of consumed webview token jti={}", jti);
                throw new AppException(ErrorCode.BAD_REQUEST, "Webview token has already been used");
            }

            return claims;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Invalid webview token: {}", e.getMessage());
            throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid or expired webview KYC token");
        }
    }
}
