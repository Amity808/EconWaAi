package org.ecom.econwaai.domain.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bank_account_audit_logs")
public class BankAccountAuditLog {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "changed_by_user_id", nullable = false)
    private UUID changedByUserId;

    @Column(name = "old_account_number")
    private String oldAccountNumber;

    @Column(name = "new_account_number", nullable = false)
    private String newAccountNumber;

    @Column(name = "old_bank_code")
    private String oldBankCode;

    @Column(name = "new_bank_code", nullable = false)
    private String newBankCode;

    @Column(name = "payout_hold_until", nullable = false)
    private Instant payoutHoldUntil;

    @Column(name = "notification_sent", nullable = false)
    private boolean notificationSent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public BankAccountAuditLog() {
    }

    public BankAccountAuditLog(
            UUID id,
            UUID orgId,
            UUID changedByUserId,
            String oldAccountNumber,
            String newAccountNumber,
            String oldBankCode,
            String newBankCode,
            Instant payoutHoldUntil,
            boolean notificationSent,
            Instant createdAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.orgId = orgId;
        this.changedByUserId = changedByUserId;
        this.oldAccountNumber = oldAccountNumber;
        this.newAccountNumber = newAccountNumber;
        this.oldBankCode = oldBankCode;
        this.newBankCode = newBankCode;
        this.payoutHoldUntil = payoutHoldUntil;
        this.notificationSent = notificationSent;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }

    public UUID getChangedByUserId() { return changedByUserId; }
    public void setChangedByUserId(UUID changedByUserId) { this.changedByUserId = changedByUserId; }

    public String getOldAccountNumber() { return oldAccountNumber; }
    public void setOldAccountNumber(String oldAccountNumber) { this.oldAccountNumber = oldAccountNumber; }

    public String getNewAccountNumber() { return newAccountNumber; }
    public void setNewAccountNumber(String newAccountNumber) { this.newAccountNumber = newAccountNumber; }

    public String getOldBankCode() { return oldBankCode; }
    public void setOldBankCode(String oldBankCode) { this.oldBankCode = oldBankCode; }

    public String getNewBankCode() { return newBankCode; }
    public void setNewBankCode(String newBankCode) { this.newBankCode = newBankCode; }

    public Instant getPayoutHoldUntil() { return payoutHoldUntil; }
    public void setPayoutHoldUntil(Instant payoutHoldUntil) { this.payoutHoldUntil = payoutHoldUntil; }

    public boolean isNotificationSent() { return notificationSent; }
    public void setNotificationSent(boolean notificationSent) { this.notificationSent = notificationSent; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
