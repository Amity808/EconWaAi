package org.ecom.econwaai.domain.repository;

import org.ecom.econwaai.domain.model.OrganizationLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationLocationRepository extends JpaRepository<OrganizationLocation, UUID> {

    List<OrganizationLocation> findByOrgId(UUID orgId);

    Optional<OrganizationLocation> findFirstByOrgIdAndIsDefaultTrue(UUID orgId);
}
