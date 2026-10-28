package org.ecom.econwaai.compliance.service;

import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.common.exception.ErrorCode;
import org.ecom.econwaai.compliance.gateway.IdentityVerificationGateway;
import org.ecom.econwaai.domain.enums.KycStatus;
import org.ecom.econwaai.domain.model.Organization;
import org.ecom.econwaai.domain.repository.OrganizationRepository;
import org.ecom.econwaai.web.dto.CacVerificationRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CacVerificationService {

    private static final Logger log = LoggerFactory.getLogger(CacVerificationService.class);

    private final ConsentService consentService;
    private final IdentityVerificationGateway verificationGateway;
    private final OrganizationRepository organizationRepository;

    public CacVerificationService(
            ConsentService consentService,
            IdentityVerificationGateway verificationGateway,
            OrganizationRepository organizationRepository
    ) {
        this.consentService = consentService;
        this.verificationGateway = verificationGateway;
        this.organizationRepository = organizationRepository;
    }

    /**
     * Validates corporate entity with CAC registry under NDPA explicit consent.
     */
    @Transactional
    public Organization verifyCac(UUID userId, UUID orgId, CacVerificationRequestDto request) {
        consentService.assertConsentGiven(userId, "NDPA_CORPORATE_VERIFICATION");

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Organization not found: " + orgId));

        IdentityVerificationGateway.CacLookupResult lookupResult =
                verificationGateway.verifyCac(request.cacNumber(), request.companyName());

        if (!lookupResult.verified()) {
            log.warn("CAC lookup failed for org {} cacNumber={}", orgId, request.cacNumber());
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "CAC registration lookup failed: Entity not found or not active in registry");
        }

        organization.setCacNumber(lookupResult.rcNumber());
        organization.setLegalName(lookupResult.companyName());
        if (organization.getKycStatus() == KycStatus.UNVERIFIED || organization.getKycStatus() == KycStatus.TIER_1_BASIC) {
            organization.setKycStatus(KycStatus.TIER_2_NIN_CAC);
            organization.setKycVerifiedAt(Instant.now());
        }

        Organization saved = organizationRepository.save(organization);
        log.info("CAC successfully verified for org={} cacNumber={} kycStatus={}",
                orgId, lookupResult.rcNumber(), saved.getKycStatus());
        return saved;
    }
}
