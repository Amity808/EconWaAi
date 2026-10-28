package org.ecom.econwaai.compliance;

import io.jsonwebtoken.Claims;
import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.compliance.service.WebviewTokenService;
import org.ecom.econwaai.web.dto.WebviewUrlResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebviewTokenServiceTest {

    private WebviewTokenService webviewTokenService;

    @BeforeEach
    void setUp() {
        webviewTokenService = new WebviewTokenService(
                "SuperSecretKeyForTestingEconWaWebviewTokensMustBeAtLeast32Bytes!",
                "https://app.econwa.ng/kyc/webview"
        );
    }

    @Test
    @DisplayName("Should generate signed webview URL with 15-minute expiration for WhatsApp handoff")
    void testGenerateWebviewUrl() {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        WebviewUrlResponseDto response = webviewTokenService.generateWebviewUrl(
                userId,
                orgId,
                "LIVENESS",
                "https://wa.me/2348000000000"
        );

        assertThat(response).isNotNull();
        assertThat(response.webviewUrl()).startsWith("https://app.econwa.ng/kyc/webview?token=");
        assertThat(response.token()).isNotBlank();
        assertThat(response.ttlSeconds()).isEqualTo(900);
        assertThat(response.expiresAt()).isNotNull();
    }

    @Test
    @DisplayName("Should validate claims on first use and reject replay attempts (single-use enforcement)")
    void testSingleUseTokenConsumption() {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        WebviewUrlResponseDto response = webviewTokenService.generateWebviewUrl(
                userId,
                orgId,
                "NIN",
                ""
        );

        // First use: must succeed and extract claims
        Claims claims = webviewTokenService.validateAndConsumeToken(response.token());
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("org_id")).isEqualTo(orgId.toString());
        assertThat(claims.get("flow_type")).isEqualTo("NIN");

        // Second use (replay attack): must fail
        assertThatThrownBy(() -> webviewTokenService.validateAndConsumeToken(response.token()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("already been used");
    }
}
