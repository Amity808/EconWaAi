package org.ecom.econwaai.domain.model;

import jakarta.persistence.*;
import org.ecom.econwaai.domain.enums.ConsentChannel;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consents")
public class Consent {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "consent_type", nullable = false)
    private String consentType;

    @Column(name = "consent_text", nullable = false)
    private String consentText;

    @Column(name = "granted_ip")
    private String grantedIp;

    @Enumerated(EnumType.STRING)
    @Column(name = "granted_channel", nullable = false)
    private ConsentChannel grantedChannel;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public Consent() {
    }

    public Consent(
            UUID id,
            UUID userId,
            UUID orgId,
            String consentType,
            String consentText,
            String grantedIp,
            ConsentChannel grantedChannel,
            Instant grantedAt,
            Instant revokedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.userId = userId;
        this.orgId = orgId;
        this.consentType = consentType;
        this.consentText = consentText;
        this.grantedIp = grantedIp;
        this.grantedChannel = grantedChannel;
        this.grantedAt = grantedAt != null ? grantedAt : Instant.now();
        this.revokedAt = revokedAt;
    }

    public static Consent grant(
            UUID userId,
            UUID orgId,
            String consentType,
            String consentText,
            String grantedIp,
            ConsentChannel grantedChannel
    ) {
        return new Consent(
                UUID.randomUUID(),
                userId,
                orgId,
                consentType,
                consentText,
                grantedIp,
                grantedChannel,
                Instant.now(),
                null
        );
    }

    /**
     * JPA lifecycle callback executed automatically before a new consent record is inserted into the database.
     * Ensures default UUID primary key and grantedAt timestamp are populated.
     */
    @PrePersist
    protected void onCreate() {
        // If an explicit UUID wasn't assigned, generate a new random UUID
        if (id == null) {
            id = UUID.randomUUID();
        }
        // Set grantedAt timestamp to current UTC time if not already provided
        if (grantedAt == null) {
            grantedAt = Instant.now();
        }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }

    public String getConsentType() { return consentType; }
    public void setConsentType(String consentType) { this.consentType = consentType; }

    public String getConsentText() { return consentText; }
    public void setConsentText(String consentText) { this.consentText = consentText; }

    public String getGrantedIp() { return grantedIp; }
    public void setGrantedIp(String grantedIp) { this.grantedIp = grantedIp; }

    public ConsentChannel getGrantedChannel() { return grantedChannel; }
    public void setGrantedChannel(ConsentChannel grantedChannel) { this.grantedChannel = grantedChannel; }

    public Instant getGrantedAt() { return grantedAt; }
    public void setGrantedAt(Instant grantedAt) { this.grantedAt = grantedAt; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
}
