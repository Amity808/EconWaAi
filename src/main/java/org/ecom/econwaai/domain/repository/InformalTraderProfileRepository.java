package org.ecom.econwaai.domain.repository;

import org.ecom.econwaai.domain.model.InformalTraderProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InformalTraderProfileRepository extends JpaRepository<InformalTraderProfile, UUID> {

    Optional<InformalTraderProfile> findByOrgId(UUID orgId);

    List<InformalTraderProfile> findAllByVerificationStatus(String verificationStatus);
}
