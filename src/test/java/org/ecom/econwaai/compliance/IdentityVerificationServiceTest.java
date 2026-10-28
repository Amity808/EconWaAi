package org.ecom.econwaai.compliance;

import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.common.util.HashUtil;
import org.ecom.econwaai.compliance.gateway.IdentityVerificationGateway;
import org.ecom.econwaai.compliance.service.ConsentService;
import org.ecom.econwaai.compliance.service.IdentityVerificationService;
import org.ecom.econwaai.domain.enums.KycStatus;
import org.ecom.econwaai.domain.model.IdentityVerification;
import org.ecom.econwaai.domain.model.Organization;
import org.ecom.econwaai.domain.repository.IdentityVerificationRepository;
import org.ecom.econwaai.domain.repository.OrganizationRepository;
import org.ecom.econwaai.web.dto.LivenessCallbackDto;
import org.ecom.econwaai.web.dto.NinVerificationRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationServiceTest {

    @Mock
    private ConsentService consentService;

    @Mock
    private IdentityVerificationGateway verificationGateway;

    @Mock
    private IdentityVerificationRepository verificationRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    private IdentityVerificationService identityVerificationService;

    @BeforeEach
    void setUp() {
        identityVerificationService = new IdentityVerificationService(
                consentService,
                verificationGateway,
                verificationRepository,
                organizationRepository
        );
    }

    @Test
    @DisplayName("Should strictly enforce ZERO-RAW storage rule and hash NIN under NDPA 2023")
    void testZeroRawStorageNinVerification() {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        String rawNin = "12345678901";
        String expectedHash = HashUtil.sha256Hex(rawNin);

        NinVerificationRequestDto request = new NinVerificationRequestDto(
                rawNin,
                "Adebayo",
                "Ogunlesi",
                "1985-06-15"
        );

        Organization org = new Organization();
        org.setId(orgId);
        org.setKycStatus(KycStatus.UNVERIFIED);

        doNothing().when(consentService).assertConsentGiven(userId, "NDPA_IDENTITY_VERIFICATION");
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(verificationGateway.verifyNin(rawNin, "Adebayo", "Ogunlesi", "1985-06-15"))
                .thenReturn(new IdentityVerificationGateway.VerificationResult("PREM-REF-001", true, true, "OK"));
        when(verificationRepository.save(any(IdentityVerification.class))).thenAnswer(i -> i.getArgument(0));

        IdentityVerification result = identityVerificationService.verifyNin(userId, orgId, request);

        // Verify entity persisted in repository
        ArgumentCaptor<IdentityVerification> captor = ArgumentCaptor.forClass(IdentityVerification.class);
        verify(verificationRepository).save(captor.capture());
        IdentityVerification captured = captor.getValue();

        // STRICT NDPA CHECK: The raw 11-digit NIN must NEVER be stored in the entity
        assertThat(captured.getIdentifierHash()).isEqualTo(expectedHash);
        assertThat(captured.getIdentifierHash()).isNotEqualTo(rawNin);
        assertThat(captured.getProviderReference()).isEqualTo("PREM-REF-001");
        assertThat(captured.getStatus()).isEqualTo("pending_liveness");

        // Org KYC status upgraded to TIER_2_NIN_CAC
        assertThat(org.getKycStatus()).isEqualTo(KycStatus.TIER_2_NIN_CAC);
        verify(organizationRepository).save(org);
    }

    @Test
    @DisplayName("Should throw exception if consent is not granted prior to NIN check")
    void testMissingConsentFailsVerification() {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        NinVerificationRequestDto request = new NinVerificationRequestDto(
                "12345678901",
                "Chidi",
                "Mbah",
                "1990-01-01"
        );

        doThrow(new AppException(org.ecom.econwaai.common.exception.ErrorCode.FORBIDDEN, "Consent required"))
                .when(consentService).assertConsentGiven(userId, "NDPA_IDENTITY_VERIFICATION");

        assertThatThrownBy(() -> identityVerificationService.verifyNin(userId, orgId, request))
                .isInstanceOf(AppException.class);

        verifyNoInteractions(verificationGateway);
        verifyNoInteractions(verificationRepository);
    }

    @Test
    @DisplayName("Should elevate organization to TIER_3_LIVENESS_INSPECTED when liveness score >= 0.85")
    void testLivenessCallbackPassesAndElevatesOrg() {
        UUID orgId = UUID.randomUUID();
        String providerRef = "PREM-REF-001";

        IdentityVerification pending = new IdentityVerification();
        pending.setId(UUID.randomUUID());
        pending.setOrgId(orgId);
        pending.setProviderReference(providerRef);
        pending.setStatus("pending_liveness");

        Organization org = new Organization();
        org.setId(orgId);
        org.setKycStatus(KycStatus.TIER_2_NIN_CAC);

        when(verificationRepository.findByProviderReference(providerRef)).thenReturn(Optional.of(pending));
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(verificationRepository.save(any(IdentityVerification.class))).thenAnswer(i -> i.getArgument(0));

        LivenessCallbackDto callback = new LivenessCallbackDto(
                providerRef,
                "PASSED",
                new BigDecimal("0.942"),
                null
        );

        IdentityVerification updated = identityVerificationService.handleLivenessCallback(callback);

        assertThat(updated.getStatus()).isEqualTo("verified");
        assertThat(updated.getLivenessScore()).isEqualTo(new BigDecimal("0.942"));
        assertThat(org.getKycStatus()).isEqualTo(KycStatus.TIER_3_LIVENESS_INSPECTED);
        verify(organizationRepository).save(org);
    }
}
