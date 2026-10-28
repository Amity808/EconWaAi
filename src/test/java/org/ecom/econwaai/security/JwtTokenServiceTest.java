package org.ecom.econwaai.security;

import org.ecom.econwaai.domain.enums.OrgType;
import org.ecom.econwaai.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService(TEST_SECRET);
    }

    @Test
    @DisplayName("Access token generates and validates with correct claims")
    void testAccessTokenClaims() {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        String token = jwtTokenService.generateAccessToken(userId, orgId, OrgType.BUYER, UserRole.PROCUREMENT_OFFICER);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenService.validateToken(token)).isTrue();
        assertThat(jwtTokenService.getUserId(token)).isEqualTo(userId);
        assertThat(jwtTokenService.getOrgId(token)).isEqualTo(orgId);
        assertThat(jwtTokenService.getOrgType(token)).isEqualTo("BUYER");
        assertThat(jwtTokenService.getRole(token)).isEqualTo("PROCUREMENT_OFFICER");
        assertThat(jwtTokenService.getTokenType(token)).isEqualTo("ACCESS");
        assertThat(jwtTokenService.isRefreshToken(token)).isFalse();
    }

    @Test
    @DisplayName("Refresh token generates and validates as REFRESH token type")
    void testRefreshToken() {
        UUID userId = UUID.randomUUID();
        String refreshToken = jwtTokenService.generateRefreshToken(userId);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtTokenService.validateToken(refreshToken)).isTrue();
        assertThat(jwtTokenService.getUserId(refreshToken)).isEqualTo(userId);
        assertThat(jwtTokenService.isRefreshToken(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("Tampered token fails validation")
    void testTamperedTokenFails() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenService.generateAccessToken(userId, null, null, UserRole.DRIVER);

        String tampered = token.substring(0, token.length() - 4) + "abcd";
        assertThat(jwtTokenService.validateToken(tampered)).isFalse();
    }
}
