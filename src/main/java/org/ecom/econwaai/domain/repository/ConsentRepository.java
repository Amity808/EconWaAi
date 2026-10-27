package org.ecom.econwaai.domain.repository;

import org.ecom.econwaai.domain.model.Consent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, UUID> {

    Optional<Consent> findByUserIdAndConsentTypeAndRevokedAtIsNull(UUID userId, String consentType);

    List<Consent> findAllByOrgId(UUID orgId);

    List<Consent> findAllByUserId(UUID userId);
}
