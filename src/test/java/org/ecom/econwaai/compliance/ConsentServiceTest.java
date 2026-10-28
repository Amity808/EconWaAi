package org.ecom.econwaai.compliance;

import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.common.exception.ErrorCode;
import org.ecom.econwaai.compliance.service.ConsentService;
import org.ecom.econwaai.domain.enums.ConsentChannel;
import org.ecom.econwaai.domain.model.Consent;
import org.ecom.econwaai.domain.repository.ConsentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {

    @Mock
    private ConsentRepository consentRepository;

    private ConsentService consentService;

    @BeforeEach
    void setUp() {
        consentService = new ConsentService(consentRepository);
    }

    @Test
    @DisplayName("Should record NDPA 2023 consent with channel and IP")
    void testRecordConsent() {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        when(consentRepository.findByUserIdAndConsentTypeAndRevokedAtIsNull(userId, "NDPA_IDENTITY_VERIFICATION"))
                .thenReturn(Optional.empty());
        when(consentRepository.save(any(Consent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Consent consent = consentService.recordConsent(
                userId,
                orgId,
                "NDPA_IDENTITY_VERIFICATION",
                "I agree to NIN tokenized verification",
                "197.210.65.12",
                ConsentChannel.WHATSAPP_OTP
        );

        assertThat(consent).isNotNull();
        assertThat(consent.getUserId()).isEqualTo(userId);
        assertThat(consent.getOrgId()).isEqualTo(orgId);
        assertThat(consent.getConsentType()).isEqualTo("NDPA_IDENTITY_VERIFICATION");
        assertThat(consent.getGrantedChannel()).isEqualTo(ConsentChannel.WHATSAPP_OTP);
        assertThat(consent.getGrantedIp()).isEqualTo("197.210.65.12");
        assertThat(consent.getRevokedAt()).isNull();
    }

    @Test
    @DisplayName("Should assert consent passes when active consent exists")
    void testAssertConsentGivenSuccess() {
        UUID userId = UUID.randomUUID();
        Consent active = new Consent();
        active.setUserId(userId);
        active.setConsentType("NDPA_IDENTITY_VERIFICATION");

        when(consentRepository.findByUserIdAndConsentTypeAndRevokedAtIsNull(userId, "NDPA_IDENTITY_VERIFICATION"))
                .thenReturn(Optional.of(active));

        // Should not throw
        consentService.assertConsentGiven(userId, "NDPA_IDENTITY_VERIFICATION");
    }

    @Test
    @DisplayName("Should throw FORBIDDEN (403) when required NDPA consent is missing")
    void testAssertConsentGivenMissingThrowsForbidden() {
        UUID userId = UUID.randomUUID();
        when(consentRepository.findByUserIdAndConsentTypeAndRevokedAtIsNull(userId, "NDPA_IDENTITY_VERIFICATION"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> consentService.assertConsentGiven(userId, "NDPA_IDENTITY_VERIFICATION"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                });
    }
}
