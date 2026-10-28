package org.ecom.econwaai.compliance.service;

import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.common.exception.ErrorCode;
import org.ecom.econwaai.common.util.HashUtil;
import org.ecom.econwaai.compliance.gateway.IdentityVerificationGateway;
import org.ecom.econwaai.domain.enums.KycStatus;
import org.ecom.econwaai.domain.model.IdentityVerification;
import org.ecom.econwaai.domain.model.Organization;
import org.ecom.econwaai.domain.repository.IdentityVerificationRepository;
import org.ecom.econwaai.domain.repository.OrganizationRepository;
import org.ecom.econwaai.web.dto.LivenessCallbackDto;
import org.ecom.econwaai.web.dto.NinVerificationRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class IdentityVerificationService {

    private static final Logger log = LoggerFactory.getLogger(IdentityVerificationService.class);
    private static final BigDecimal LIVENESS_PASS_THRESHOLD = new BigDecimal("0.850");

    private final ConsentService consentService;
    private final IdentityVerificationGateway verificationGateway;
    private final IdentityVerificationRepository verificationRepository;
    private final OrganizationRepository organizationRepository;

    public IdentityVerificationService(
            ConsentService consentService,
            IdentityVerificationGateway verificationGateway,
            IdentityVerificationRepository verificationRepository,
            OrganizationRepository organizationRepository
    ) {
        this.consentService = consentService;
        this.verificationGateway = verificationGateway;
        this.verificationRepository = verificationRepository;
        this.organizationRepository = organizationRepository;
    }

    /**
     * Executes tokenized NIN verification under NDPA 2023 with strictly ZERO raw storage of national IDs.
     */
    @Transactional
    public IdentityVerification verifyNin(UUID userId, UUID orgId, NinVerificationRequestDto request) {
        // 1. Mandatory NDPA 2023 explicit consent check
        consentService.assertConsentGiven(userId, "NDPA_IDENTITY_VERIFICATION");

        // 2. Compute one-way cryptographic SHA-256 hash (never persist raw NIN!)
        String ninHash = HashUtil.sha256Hex(request.nin());

        // 3. Call upstream identity partner
        IdentityVerificationGateway.VerificationResult result = verificationGateway.verifyNin(
                request.nin(),
                request.firstName(),
                request.lastName(),
                request.dateOfBirth()
        );

        if (!result.verified()) {
            log.warn("NIN verification failed for user {} with ref {}: {}", userId, result.providerReference(), result.message());
            IdentityVerification failedRecord = new IdentityVerification(
                    UUID.randomUUID(),
                    orgId,
                    userId,
                    "NIN",
                    ninHash,
                    result.providerReference(),
                    "rejected",
                    null,
                    null,
                    Instant.now()
            );
            verificationRepository.save(failedRecord);
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "NIN verification rejected: " + result.message());
        }

        // 4. Save tokenized verification record
        String status = result.livenessRequired() ? "pending_liveness" : "verified";
        IdentityVerification verification = new IdentityVerification(
                UUID.randomUUID(),
                orgId,
                userId,
                "NIN",
                ninHash,
                result.providerReference(),
                status,
                null,
                result.livenessRequired() ? null : Instant.now(),
                Instant.now()
        );
        IdentityVerification saved = verificationRepository.save(verification);

        // 5. Update Organization KYC Status to at least TIER_2_NIN_CAC if not already higher
        organizationRepository.findById(orgId).ifPresent(org -> {
            if (org.getKycStatus() == KycStatus.UNVERIFIED || org.getKycStatus() == KycStatus.TIER_1_BASIC) {
                org.setKycStatus(KycStatus.TIER_2_NIN_CAC);
                org.setKycVerifiedAt(Instant.now());
                organizationRepository.save(org);
                log.info("Elevated organization {} to TIER_2_NIN_CAC via tokenized NIN verification", orgId);
            }
        });

        log.info("NIN verification tokenized record created id={} providerRef={} status={}",
                saved.getId(), result.providerReference(), status);
        return saved;
    }

    /**
     * Handles 3D selfie liveness check callback from Prembly / Smile ID.
     * When liveness score meets threshold (>= 0.85), elevates organization to TIER_3_LIVENESS_INSPECTED.
     */
    @Transactional
    public IdentityVerification handleLivenessCallback(LivenessCallbackDto callback) {
        IdentityVerification record = verificationRepository.findByProviderReference(callback.providerReference())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND,
                        "Identity verification record not found for reference: " + callback.providerReference()));

        record.setLivenessScore(callback.livenessScore());

        boolean passed = "PASSED".equalsIgnoreCase(callback.status())
                && callback.livenessScore() != null
                && callback.livenessScore().compareTo(LIVENESS_PASS_THRESHOLD) >= 0;

        if (passed) {
            record.setStatus("verified");
            record.setVerifiedAt(Instant.now());

            // Elevate organization KYC status to TIER_3_LIVENESS_INSPECTED
            organizationRepository.findById(record.getOrgId()).ifPresent(org -> {
                org.setKycStatus(KycStatus.TIER_3_LIVENESS_INSPECTED);
                org.setKycVerifiedAt(Instant.now());
                organizationRepository.save(org);
                log.info("Organization {} upgraded to TIER_3_LIVENESS_INSPECTED (score={})",
                        org.getId(), callback.livenessScore());
            });
        } else {
            record.setStatus("rejected");
            log.warn("Liveness check failed for verification id={} ref={} score={}",
                    record.getId(), callback.providerReference(), callback.livenessScore());
        }

        return verificationRepository.save(record);
    }
}
