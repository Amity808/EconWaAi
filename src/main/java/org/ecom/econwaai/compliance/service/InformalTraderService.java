package org.ecom.econwaai.compliance.service;

import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.common.exception.ErrorCode;
import org.ecom.econwaai.common.util.HashUtil;
import org.ecom.econwaai.domain.enums.KycStatus;
import org.ecom.econwaai.domain.model.InformalTraderProfile;
import org.ecom.econwaai.domain.model.Organization;
import org.ecom.econwaai.domain.repository.InformalTraderProfileRepository;
import org.ecom.econwaai.domain.repository.OrganizationRepository;
import org.ecom.econwaai.web.dto.InformalTraderRegistrationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class InformalTraderService {

    private static final Logger log = LoggerFactory.getLogger(InformalTraderService.class);

    private final ConsentService consentService;
    private final InformalTraderProfileRepository profileRepository;
    private final OrganizationRepository organizationRepository;

    public InformalTraderService(
            ConsentService consentService,
            InformalTraderProfileRepository profileRepository,
            OrganizationRepository organizationRepository
    ) {
        this.consentService = consentService;
        this.profileRepository = profileRepository;
        this.organizationRepository = organizationRepository;
    }

    /**
     * Submits an informal trader alternative verification profile with market leader endorsement and guarantor.
     * Enforces NDPA 2023 zero-raw storage of guarantor NIN and BVN identifiers.
     */
    @Transactional
    public InformalTraderProfile submitProfile(UUID userId, UUID orgId, InformalTraderRegistrationDto request) {
        consentService.assertConsentGiven(userId, "NDPA_INFORMAL_TRADER_VERIFICATION");

        // Hash guarantor identifiers if present (Strict Zero-Raw Storage)
        String guarantorNinHash = HashUtil.sha256Hex(request.guarantorNin());
        String guarantorBvnHash = HashUtil.sha256Hex(request.guarantorBvn());

        InformalTraderProfile profile = profileRepository.findByOrgId(orgId).orElseGet(InformalTraderProfile::new);

        profile.setOrgId(orgId);
        profile.setMarketName(request.marketName().trim());
        profile.setTradeAssociationName(request.tradeAssociationName().trim());
        profile.setTradeAssociationRegNumber(request.tradeAssociationRegNumber());
        profile.setMarketLeaderName(request.marketLeaderName().trim());
        profile.setMarketLeaderPhone(request.marketLeaderPhone().trim());
        profile.setMarketLeaderEndorsementLetterUrl(request.marketLeaderEndorsementLetterUrl());
        profile.setGuarantorFullName(request.guarantorFullName().trim());
        profile.setGuarantorPhone(request.guarantorPhone().trim());
        profile.setGuarantorNinHash(guarantorNinHash);
        profile.setGuarantorBvnHash(guarantorBvnHash);
        profile.setVerificationStatus("pending_review");

        InformalTraderProfile saved = profileRepository.save(profile);
        log.info("Submitted informal trader profile id={} for org={}", saved.getId(), orgId);
        return saved;
    }

    /**
     * Platform admin review and approval for informal trader profile, upgrading organization to TIER_1_BASIC.
     */
    @Transactional
    public InformalTraderProfile approveProfile(UUID orgId, UUID adminUserId) {
        InformalTraderProfile profile = profileRepository.findByOrgId(orgId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Informal trader profile not found for org: " + orgId));

        profile.setVerificationStatus("verified");
        profile.setReviewedByAdminId(adminUserId);
        InformalTraderProfile saved = profileRepository.save(profile);

        organizationRepository.findById(orgId).ifPresent(org -> {
            if (org.getKycStatus() == KycStatus.UNVERIFIED) {
                org.setKycStatus(KycStatus.TIER_1_BASIC);
                org.setKycVerifiedAt(Instant.now());
                organizationRepository.save(org);
                log.info("Elevated org {} to TIER_1_BASIC via informal trader endorsement", orgId);
            }
        });

        return saved;
    }
}
