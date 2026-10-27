package org.ecom.econwaai.domain.repository;

import org.ecom.econwaai.domain.enums.OrgType;
import org.ecom.econwaai.domain.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByCacNumber(String cacNumber);

    Optional<Organization> findByPrimaryPhone(String primaryPhone);

    List<Organization> findAllByOrgType(OrgType orgType);
}
