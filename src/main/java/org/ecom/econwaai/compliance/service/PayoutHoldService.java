package org.ecom.econwaai.compliance.service;

import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.common.exception.ErrorCode;
import org.ecom.econwaai.domain.model.BankAccountAuditLog;
import org.ecom.econwaai.domain.model.Organization;
import org.ecom.econwaai.domain.repository.BankAccountAuditLogRepository;
import org.ecom.econwaai.domain.repository.OrganizationRepository;
import org.ecom.econwaai.web.dto.UpdateBankAccountDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class PayoutHoldService {

    private static final Logger log = LoggerFactory.getLogger(PayoutHoldService.class);
    public static final Duration PAYOUT_HOLD_DURATION = Duration.ofHours(48);

    private final OrganizationRepository organizationRepository;
    private final BankAccountAuditLogRepository auditLogRepository;
    private final AdminNotificationService adminNotificationService;

    public PayoutHoldService(
            OrganizationRepository organizationRepository,
            BankAccountAuditLogRepository auditLogRepository,
            AdminNotificationService adminNotificationService
    ) {
        this.organizationRepository = organizationRepository;
        this.auditLogRepository = auditLogRepository;
        this.adminNotificationService = adminNotificationService;
    }

    /**
     * Updates organization bank account details and automatically applies a mandatory 48-hour anti-fraud payout hold.
     */
    @Transactional
    public BankAccountAuditLog updateBankAccount(UUID orgId, UUID userId, UpdateBankAccountDto dto, String currentAccount, String currentBankCode) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Organization not found: " + orgId));

        Instant payoutHoldUntil = Instant.now().plus(PAYOUT_HOLD_DURATION);
        organization.setPayoutHoldUntil(payoutHoldUntil);
        organizationRepository.save(organization);

        BankAccountAuditLog auditLog = new BankAccountAuditLog(
                UUID.randomUUID(),
                orgId,
                userId,
                currentAccount,
                dto.accountNumber(),
                currentBankCode,
                dto.bankCode(),
                payoutHoldUntil,
                true,
                Instant.now()
        );
        BankAccountAuditLog savedLog = auditLogRepository.save(auditLog);

        adminNotificationService.notifyBankAccountChange(
                orgId,
                userId,
                currentAccount,
                dto.accountNumber(),
                dto.bankCode(),
                payoutHoldUntil
        );

        log.info("Activated 48-hour anti-fraud payout hold on org={} until={}", orgId, payoutHoldUntil);
        return savedLog;
    }

    /**
     * Checks if payouts are currently frozen for this organization due to recent bank detail changes.
     */
    @Transactional(readOnly = true)
    public boolean isPayoutBlocked(UUID orgId) {
        return organizationRepository.findById(orgId)
                .map(org -> org.getPayoutHoldUntil() != null && org.getPayoutHoldUntil().isAfter(Instant.now()))
                .orElse(false);
    }
}
