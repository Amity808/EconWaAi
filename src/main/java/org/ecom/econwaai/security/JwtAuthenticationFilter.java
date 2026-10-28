package org.ecom.econwaai.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ecom.econwaai.common.context.OrgContext;
import org.ecom.econwaai.common.context.OrgContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * Filter that extracts the Bearer JWT token from HTTP requests, verifies the cryptographic signature,
 * and populates the OrgContextHolder and Spring SecurityContext with the tenant and user claims.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenService.validateToken(token)) {
            // Verify this is an access token, not a refresh token
            if (!jwtTokenService.isRefreshToken(token)) {
                UUID userId = jwtTokenService.getUserId(token);
                UUID orgId = jwtTokenService.getOrgId(token);
                String orgType = jwtTokenService.getOrgType(token);
                String role = jwtTokenService.getRole(token);

                String correlationId = OrgContextHolder.getCorrelationId().orElse("unknown");
                OrgContext context = new OrgContext(
                        orgId,
                        orgType != null ? orgType : "anonymous",
                        userId,
                        role != null ? role : "ANONYMOUS",
                        correlationId
                );
                OrgContextHolder.set(context);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER")))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
