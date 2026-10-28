package org.ecom.econwaai.compliance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AdminNotificationService {

    private static final Logger log = LoggerFactory.getLogger(AdminNotificationService.class);

    /**
     * Dispatches high-priority security notification to platform risk and compliance operations.
     */
    public void notifyBankAccountChange(
            UUID orgId,
            UUID changedByUserId,
            String oldAccount,
            String newAccount,
            String bankCode,
            Instant payoutHoldUntil
    ) {
        log.warn("[SECURITY ALERT] Bank account details updated for organization {}. Changed by user {}. " +
                        "Old: [{}], New: [{}], BankCode: [{}]. Automated 48-hour anti-fraud payout hold active until {}",
                orgId, changedByUserId, maskAccount(oldAccount), maskAccount(newAccount), bankCode, payoutHoldUntil);
    }

    private String maskAccount(String account) {
        if (account == null || account.length() < 4) {
            return "N/A";
        }
        return "******" + account.substring(account.length() - 4);
    }
}
