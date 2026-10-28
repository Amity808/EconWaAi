package org.ecom.econwaai.domain.repository;

import org.ecom.econwaai.domain.model.BankAccountAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BankAccountAuditLogRepository extends JpaRepository<BankAccountAuditLog, UUID> {

    List<BankAccountAuditLog> findAllByOrgIdOrderByCreatedAtDesc(UUID orgId);
}
