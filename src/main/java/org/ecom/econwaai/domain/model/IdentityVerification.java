package org.ecom.econwaai.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "identity_verifications")
public class IdentityVerification {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "id_type", nullable = false)
    private String idType; // 'NIN', 'BVN', 'CAC'

    @Column(name = "identifier_hash", nullable = false)
    private String identifierHash; // SHA-256 (Never raw numbers!)

    @Column(name = "provider_reference", nullable = false)
    private String providerReference;

    @Column(name = "status", nullable = false)
    private String status; // 'verified', 'rejected', 'pending_liveness'

    @Column(name = "liveness_score", precision = 4, scale = 3)
    private BigDecimal livenessScore;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public IdentityVerification() {
    }

    public IdentityVerification(
            UUID id,
            UUID orgId,
            UUID userId,
            String idType,
            String identifierHash,
            String providerReference,
            String status,
            BigDecimal livenessScore,
            Instant verifiedAt,
            Instant createdAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.orgId = orgId;
        this.userId = userId;
        this.idType = idType;
        this.identifierHash = identifierHash;
        this.providerReference = providerReference;
        this.status = status;
        this.livenessScore = livenessScore;
        this.verifiedAt = verifiedAt;
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

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }

    public String getIdentifierHash() { return identifierHash; }
    public void setIdentifierHash(String identifierHash) { this.identifierHash = identifierHash; }

    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getLivenessScore() { return livenessScore; }
    public void setLivenessScore(BigDecimal livenessScore) { this.livenessScore = livenessScore; }

    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
