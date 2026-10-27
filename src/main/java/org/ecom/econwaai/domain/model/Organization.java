package org.ecom.econwaai.domain.model;

import jakarta.persistence.*;
import org.ecom.econwaai.domain.enums.KycStatus;
import org.ecom.econwaai.domain.enums.OrgType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "trade_name", nullable = false)
    private String tradeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "org_type", nullable = false)
    private OrgType orgType;

    @Column(name = "cac_number")
    private String cacNumber;

    @Column(name = "tin")
    private String tin;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false)
    private KycStatus kycStatus;

    @Column(name = "kyc_verified_at")
    private Instant kycVerifiedAt;

    @Column(name = "primary_phone", nullable = false)
    private String primaryPhone;

    @Column(name = "primary_email")
    private String primaryEmail;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "payout_hold_until")
    private Instant payoutHoldUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Organization() {
    }

    public Organization(
            UUID id,
            String legalName,
            String tradeName,
            OrgType orgType,
            String cacNumber,
            String tin,
            KycStatus kycStatus,
            Instant kycVerifiedAt,
            String primaryPhone,
            String primaryEmail,
            boolean isActive,
            Instant payoutHoldUntil,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.legalName = legalName;
        this.tradeName = tradeName;
        this.orgType = orgType;
        this.cacNumber = cacNumber;
        this.tin = tin;
        this.kycStatus = kycStatus != null ? kycStatus : KycStatus.UNVERIFIED;
        this.kycVerifiedAt = kycVerifiedAt;
        this.primaryPhone = primaryPhone;
        this.primaryEmail = primaryEmail;
        this.isActive = isActive;
        this.payoutHoldUntil = payoutHoldUntil;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static Organization create(
            String legalName,
            String tradeName,
            OrgType orgType,
            String cacNumber,
            String tin,
            String primaryPhone,
            String primaryEmail
    ) {
        Instant now = Instant.now();
        return new Organization(
                UUID.randomUUID(),
                legalName,
                tradeName,
                orgType,
                cacNumber,
                tin,
                KycStatus.UNVERIFIED,
                null,
                primaryPhone,
                primaryEmail,
                true,
                null,
                now,
                now
        );
    }

    /**
     * JPA lifecycle callback executed automatically before this entity is first inserted into the database.
     * Ensures default UUID primary key and creation/update audit timestamps are set.
     */
    @PrePersist
    protected void onCreate() {
        // If an explicit UUID wasn't assigned, generate a new random UUID
        if (id == null) {
            id = UUID.randomUUID();
        }
        // Set creation timestamp to current UTC time if not already provided
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        // Set initial update timestamp to match creation time
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    /**
     * JPA lifecycle callback executed automatically before an existing record is updated in the database.
     * Keeps the updatedAt audit timestamp current.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }

    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }

    public OrgType getOrgType() { return orgType; }
    public void setOrgType(OrgType orgType) { this.orgType = orgType; }

    public String getCacNumber() { return cacNumber; }
    public void setCacNumber(String cacNumber) { this.cacNumber = cacNumber; }

    public String getTin() { return tin; }
    public void setTin(String tin) { this.tin = tin; }

    public KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }

    public Instant getKycVerifiedAt() { return kycVerifiedAt; }
    public void setKycVerifiedAt(Instant kycVerifiedAt) { this.kycVerifiedAt = kycVerifiedAt; }

    public String getPrimaryPhone() { return primaryPhone; }
    public void setPrimaryPhone(String primaryPhone) { this.primaryPhone = primaryPhone; }

    public String getPrimaryEmail() { return primaryEmail; }
    public void setPrimaryEmail(String primaryEmail) { this.primaryEmail = primaryEmail; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Instant getPayoutHoldUntil() { return payoutHoldUntil; }
    public void setPayoutHoldUntil(Instant payoutHoldUntil) { this.payoutHoldUntil = payoutHoldUntil; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
