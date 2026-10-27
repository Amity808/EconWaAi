package org.ecom.econwaai.domain.model;

import jakarta.persistence.*;
import org.ecom.econwaai.domain.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public User() {
    }

    public User(
            UUID id,
            UUID orgId,
            String phoneNumber,
            String fullName,
            String email,
            String passwordHash,
            UserRole role,
            boolean isActive,
            Instant lastLoginAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.orgId = orgId;
        this.phoneNumber = phoneNumber;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static User create(
            UUID orgId,
            String phoneNumber,
            String fullName,
            String email,
            String passwordHash,
            UserRole role
    ) {
        Instant now = Instant.now();
        return new User(
                UUID.randomUUID(),
                orgId,
                phoneNumber,
                fullName,
                email,
                passwordHash,
                role,
                true,
                null,
                now,
                now
        );
    }

    /**
     * JPA lifecycle callback executed automatically before a new user is inserted into the database.
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
     * JPA lifecycle callback executed automatically before a user record is updated.
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

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
