package org.ecom.econwaai.domain.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "informal_trader_profiles")
public class InformalTraderProfile {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false, unique = true)
    private UUID orgId;

    @Column(name = "market_name", nullable = false)
    private String marketName;

    @Column(name = "trade_association_name", nullable = false)
    private String tradeAssociationName;

    @Column(name = "trade_association_reg_number")
    private String tradeAssociationRegNumber;

    @Column(name = "market_leader_name", nullable = false)
    private String marketLeaderName;

    @Column(name = "market_leader_phone", nullable = false)
    private String marketLeaderPhone;

    @Column(name = "market_leader_endorsement_letter_url")
    private String marketLeaderEndorsementLetterUrl;

    @Column(name = "guarantor_full_name", nullable = false)
    private String guarantorFullName;

    @Column(name = "guarantor_phone", nullable = false)
    private String guarantorPhone;

    @Column(name = "guarantor_nin_hash")
    private String guarantorNinHash;

    @Column(name = "guarantor_bvn_hash")
    private String guarantorBvnHash;

    @Column(name = "verification_status", nullable = false)
    private String verificationStatus; // 'pending_review', 'verified', 'rejected'

    @Column(name = "reviewed_by_admin_id")
    private UUID reviewedByAdminId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public InformalTraderProfile() {
    }

    public InformalTraderProfile(
            UUID id,
            UUID orgId,
            String marketName,
            String tradeAssociationName,
            String tradeAssociationRegNumber,
            String marketLeaderName,
            String marketLeaderPhone,
            String marketLeaderEndorsementLetterUrl,
            String guarantorFullName,
            String guarantorPhone,
            String guarantorNinHash,
            String guarantorBvnHash,
            String verificationStatus,
            UUID reviewedByAdminId,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.orgId = orgId;
        this.marketName = marketName;
        this.tradeAssociationName = tradeAssociationName;
        this.tradeAssociationRegNumber = tradeAssociationRegNumber;
        this.marketLeaderName = marketLeaderName;
        this.marketLeaderPhone = marketLeaderPhone;
        this.marketLeaderEndorsementLetterUrl = marketLeaderEndorsementLetterUrl;
        this.guarantorFullName = guarantorFullName;
        this.guarantorPhone = guarantorPhone;
        this.guarantorNinHash = guarantorNinHash;
        this.guarantorBvnHash = guarantorBvnHash;
        this.verificationStatus = verificationStatus != null ? verificationStatus : "pending_review";
        this.reviewedByAdminId = reviewedByAdminId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }

    public String getMarketName() { return marketName; }
    public void setMarketName(String marketName) { this.marketName = marketName; }

    public String getTradeAssociationName() { return tradeAssociationName; }
    public void setTradeAssociationName(String tradeAssociationName) { this.tradeAssociationName = tradeAssociationName; }

    public String getTradeAssociationRegNumber() { return tradeAssociationRegNumber; }
    public void setTradeAssociationRegNumber(String tradeAssociationRegNumber) { this.tradeAssociationRegNumber = tradeAssociationRegNumber; }

    public String getMarketLeaderName() { return marketLeaderName; }
    public void setMarketLeaderName(String marketLeaderName) { this.marketLeaderName = marketLeaderName; }

    public String getMarketLeaderPhone() { return marketLeaderPhone; }
    public void setMarketLeaderPhone(String marketLeaderPhone) { this.marketLeaderPhone = marketLeaderPhone; }

    public String getMarketLeaderEndorsementLetterUrl() { return marketLeaderEndorsementLetterUrl; }
    public void setMarketLeaderEndorsementLetterUrl(String url) { this.marketLeaderEndorsementLetterUrl = url; }

    public String getGuarantorFullName() { return guarantorFullName; }
    public void setGuarantorFullName(String guarantorFullName) { this.guarantorFullName = guarantorFullName; }

    public String getGuarantorPhone() { return guarantorPhone; }
    public void setGuarantorPhone(String guarantorPhone) { this.guarantorPhone = guarantorPhone; }

    public String getGuarantorNinHash() { return guarantorNinHash; }
    public void setGuarantorNinHash(String guarantorNinHash) { this.guarantorNinHash = guarantorNinHash; }

    public String getGuarantorBvnHash() { return guarantorBvnHash; }
    public void setGuarantorBvnHash(String guarantorBvnHash) { this.guarantorBvnHash = guarantorBvnHash; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public UUID getReviewedByAdminId() { return reviewedByAdminId; }
    public void setReviewedByAdminId(UUID reviewedByAdminId) { this.reviewedByAdminId = reviewedByAdminId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
