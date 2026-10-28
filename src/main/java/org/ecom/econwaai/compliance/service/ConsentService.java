package org.ecom.econwaai.compliance.service;

import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.common.exception.ErrorCode;
import org.ecom.econwaai.domain.enums.ConsentChannel;
import org.ecom.econwaai.domain.model.Consent;
import org.ecom.econwaai.domain.repository.ConsentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConsentService {

    private static final Logger log = LoggerFactory.getLogger(ConsentService.class);

    private final ConsentRepository consentRepository;

    public ConsentService(ConsentRepository consentRepository) {
        this.consentRepository = consentRepository;
    }

    /**
     * Records explicit legal consent under the Nigeria Data Protection Act (NDPA) 2023.
     */
    @Transactional
    public Consent recordConsent(
            UUID userId,
            UUID orgId,
            String consentType,
            String consentText,
            String clientIp,
            ConsentChannel channel
    ) {
        if (consentType == null || consentType.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Consent type is required");
        }
        if (consentText == null || consentText.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Consent text is required under NDPA 2023");
        }

        // Revoke any existing active consent for this type to maintain an immutable audit trail
        consentRepository.findByUserIdAndConsentTypeAndRevokedAtIsNull(userId, consentType)
                .ifPresent(existing -> {
                    existing.setRevokedAt(Instant.now());
                    consentRepository.save(existing);
                    log.info("Revoked previous consent id={} for user={}", existing.getId(), userId);
                });

        Consent consent = Consent.grant(
                userId,
                orgId,
                consentType.trim(),
                consentText.trim(),
                clientIp,
                channel != null ? channel : ConsentChannel.WHATSAPP_OTP
        );

        Consent saved = consentRepository.save(consent);
        log.info("Recorded NDPA consent id={} for user={} org={} type={}",
                saved.getId(), userId, orgId, consentType);
        return saved;
    }

    /**
     * Checks if a user has an active, unrevoked consent record for the specified consent type.
     */
    @Transactional(readOnly = true)
    public boolean hasActiveConsent(UUID userId, String consentType) {
        return consentRepository.findByUserIdAndConsentTypeAndRevokedAtIsNull(userId, consentType).isPresent();
    }

    /**
     * Enforces mandatory explicit consent check. Throws 403 Forbidden if consent is missing.
     */
    @Transactional(readOnly = true)
    public void assertConsentGiven(UUID userId, String consentType) {
        if (!hasActiveConsent(userId, consentType)) {
            log.warn("NDPA consent check failed: user {} lacks active consent for {}", userId, consentType);
            throw new AppException(ErrorCode.FORBIDDEN,
                    "Explicit consent for '" + consentType + "' is required under NDPA 2023 before verification");
        }
    }

    /**
     * Revokes active consent for a user.
     */
    @Transactional
    public void revokeConsent(UUID userId, String consentType) {
        Optional<Consent> consentOpt = consentRepository.findByUserIdAndConsentTypeAndRevokedAtIsNull(userId, consentType);
        if (consentOpt.isPresent()) {
            Consent consent = consentOpt.get();
            consent.setRevokedAt(Instant.now());
            consentRepository.save(consent);
            log.info("Revoked consent for user={} type={}", userId, consentType);
        }
    }
}
