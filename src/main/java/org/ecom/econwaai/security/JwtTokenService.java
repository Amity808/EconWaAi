package org.ecom.econwaai.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.common.exception.ErrorCode;
import org.ecom.econwaai.domain.enums.OrgType;
import org.ecom.econwaai.domain.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Service for generating, parsing, and validating cryptographically signed JWT tokens.
 * Emits 15-minute Access Tokens and 30-day Refresh Tokens in compliance with Week 2 spec.
 */
@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    public static final long ACCESS_TOKEN_EXPIRATION_MS = 15L * 60 * 1000; // 15 minutes
    public static final long REFRESH_TOKEN_EXPIRATION_MS = 30L * 24 * 60 * 60 * 1000; // 30 days

    private final SecretKey key;

    public JwtTokenService(
            @Value("${app.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String secret
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a short-lived 15-minute access token carrying tenant and permission claims.
     */
    public String generateAccessToken(UUID userId, UUID orgId, OrgType orgType, UserRole role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_MS);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("org_id", orgId != null ? orgId.toString() : null)
                .claim("org_type", orgType != null ? orgType.name() : null)
                .claim("role", role != null ? role.name() : "ANONYMOUS")
                .claim("token_type", "ACCESS")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Generates a long-lived 30-day refresh token for rotating sessions.
     */
    public String generateRefreshToken(UUID userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_MS);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("token_type", "REFRESH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Validates cryptographic signature and ensures token is not expired.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token has expired. Please refresh session.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid authentication token.");
        }
    }

    public UUID getUserId(String token) {
        return UUID.fromString(getClaims(token).getSubject());
    }

    public UUID getOrgId(String token) {
        String orgIdStr = getClaims(token).get("org_id", String.class);
        return orgIdStr != null ? UUID.fromString(orgIdStr) : null;
    }

    public String getOrgType(String token) {
        return getClaims(token).get("org_type", String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getTokenType(String token) {
        return getClaims(token).get("token_type", String.class);
    }

    public boolean isRefreshToken(String token) {
        return "REFRESH".equalsIgnoreCase(getTokenType(token));
    }
}
