package org.ecom.econwaai.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organization_locations")
public class OrganizationLocation {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "market_name")
    private String marketName;

    @Column(name = "section_or_line")
    private String sectionOrLine;

    @Column(name = "shop_or_plaza_number")
    private String shopOrPlazaNumber;

    @Column(name = "landmark_description", nullable = false)
    private String landmarkDescription;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "gps_accuracy_meters", precision = 6, scale = 2)
    private BigDecimal gpsAccuracyMeters;

    @Column(name = "onsite_contact_name", nullable = false)
    private String onsiteContactName;

    @Column(name = "onsite_contact_phone", nullable = false)
    private String onsiteContactPhone;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "status", nullable = false)
    private String status = "active";

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "operating_hours")
    private String operatingHours;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public OrganizationLocation() {
    }

    public OrganizationLocation(
            UUID id,
            UUID orgId,
            String label,
            String marketName,
            String sectionOrLine,
            String shopOrPlazaNumber,
            String landmarkDescription,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal gpsAccuracyMeters,
            String onsiteContactName,
            String onsiteContactPhone,
            boolean isDefault,
            String status,
            boolean isActive,
            String operatingHours,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.orgId = orgId;
        this.label = label;
        this.marketName = marketName;
        this.sectionOrLine = sectionOrLine;
        this.shopOrPlazaNumber = shopOrPlazaNumber;
        this.landmarkDescription = landmarkDescription;
        this.latitude = latitude;
        this.longitude = longitude;
        this.gpsAccuracyMeters = gpsAccuracyMeters;
        this.onsiteContactName = onsiteContactName;
        this.onsiteContactPhone = onsiteContactPhone;
        this.isDefault = isDefault;
        this.status = status != null ? status : "active";
        this.isActive = isActive;
        this.operatingHours = operatingHours;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static OrganizationLocation create(
            UUID orgId,
            String label,
            String marketName,
            String sectionOrLine,
            String shopOrPlazaNumber,
            String landmarkDescription,
            BigDecimal latitude,
            BigDecimal longitude,
            String onsiteContactName,
            String onsiteContactPhone,
            boolean isDefault
    ) {
        Instant now = Instant.now();
        return new OrganizationLocation(
                UUID.randomUUID(),
                orgId,
                label,
                marketName,
                sectionOrLine,
                shopOrPlazaNumber,
                landmarkDescription,
                latitude,
                longitude,
                null,
                onsiteContactName,
                onsiteContactPhone,
                isDefault,
                "active",
                true,
                "{\"open\": \"07:00\", \"close\": \"18:00\", \"closed_days\": [0]}",
                now,
                now
        );
    }

    /**
     * JPA lifecycle callback executed automatically before this location is first inserted into the database.
     * Ensures default UUID primary key and audit timestamps are populated.
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
     * JPA lifecycle callback executed automatically before this location record is updated.
     * Updates the updatedAt audit timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getMarketName() { return marketName; }
    public void setMarketName(String marketName) { this.marketName = marketName; }

    public String getSectionOrLine() { return sectionOrLine; }
    public void setSectionOrLine(String sectionOrLine) { this.sectionOrLine = sectionOrLine; }

    public String getShopOrPlazaNumber() { return shopOrPlazaNumber; }
    public void setShopOrPlazaNumber(String shopOrPlazaNumber) { this.shopOrPlazaNumber = shopOrPlazaNumber; }

    public String getLandmarkDescription() { return landmarkDescription; }
    public void setLandmarkDescription(String landmarkDescription) { this.landmarkDescription = landmarkDescription; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public BigDecimal getGpsAccuracyMeters() { return gpsAccuracyMeters; }
    public void setGpsAccuracyMeters(BigDecimal gpsAccuracyMeters) { this.gpsAccuracyMeters = gpsAccuracyMeters; }

    public String getOnsiteContactName() { return onsiteContactName; }
    public void setOnsiteContactName(String onsiteContactName) { this.onsiteContactName = onsiteContactName; }

    public String getOnsiteContactPhone() { return onsiteContactPhone; }
    public void setOnsiteContactPhone(String onsiteContactPhone) { this.onsiteContactPhone = onsiteContactPhone; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getOperatingHours() { return operatingHours; }
    public void setOperatingHours(String operatingHours) { this.operatingHours = operatingHours; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
