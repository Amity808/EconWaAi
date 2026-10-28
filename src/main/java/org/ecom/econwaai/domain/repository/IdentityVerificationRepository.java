package org.ecom.econwaai.domain.repository;

import org.ecom.econwaai.domain.model.IdentityVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdentityVerificationRepository extends JpaRepository<IdentityVerification, UUID> {

    List<IdentityVerification> findAllByOrgId(UUID orgId);

    List<IdentityVerification> findAllByUserId(UUID userId);

    Optional<IdentityVerification> findByProviderReference(String providerReference);

    Optional<IdentityVerification> findByIdentifierHashAndIdType(String identifierHash, String idType);
}
