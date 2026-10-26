package com.greenocean.backend.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token_hash", nullable = false, length = 255)
    private String refreshTokenHash;

    @Column(name = "device_name", length = 150)
    private String deviceName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name="client_type",nullable=false) private String clientType="MOBILE";
    @Column(name="session_scope",nullable=false) private String sessionScope="MEMBER";
    @Column(name="overall_expires_at",nullable=false) private Instant overallExpiresAt;
    @Column(name="last_activity_at",nullable=false) private Instant lastActivityAt;
    @Column(name="idle_expires_at",nullable=false) private Instant idleExpiresAt;
    @Column(name="reauthenticated_at") private Instant reauthenticatedAt;
    @Column(name="mfa_authenticated_at") private Instant mfaAuthenticatedAt;

    @Column(name="token_version",nullable=false) private int tokenVersion;
    protected UserSession() {
    }

    public UserSession(UUID id, User user, String refreshTokenHash, String deviceName, Instant expiresAt) {
        this.id = id;
        this.user = user;
        this.refreshTokenHash = refreshTokenHash;
        this.deviceName = deviceName;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
        this.overallExpiresAt=expiresAt;this.lastActivityAt=createdAt;this.idleExpiresAt=com.greenocean.backend.auth.service.SessionPolicy.earlier(expiresAt,createdAt.plus(java.time.Duration.ofDays(1)));
    }

    public boolean isUsableAt(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now) && overallExpiresAt.isAfter(now) && idleExpiresAt.isAfter(now);
    }

    public void rotate(String newRefreshTokenHash, Instant newExpiresAt) {
        this.refreshTokenHash = newRefreshTokenHash;
        this.expiresAt = com.greenocean.backend.auth.service.SessionPolicy.earlier(newExpiresAt,overallExpiresAt);
        // Refresh is automatic traffic, not evidence of meaningful user activity.
    }

    public void revoke() {
        if (revokedAt == null) {
            revokedAt = Instant.now();
        }
    }

    public void initialize(String client,String scope,Instant now,com.greenocean.backend.auth.service.SessionPolicy policy){
      clientType=client;sessionScope=scope;createdAt=now;lastActivityAt=now;lastUsedAt=now;
      overallExpiresAt=now.plus(policy.overall(client,scope));expiresAt=com.greenocean.backend.auth.service.SessionPolicy.earlier(expiresAt,overallExpiresAt);
      idleExpiresAt=com.greenocean.backend.auth.service.SessionPolicy.earlier(now.plus(policy.idle(client,scope)),overallExpiresAt);reauthenticatedAt=now;
    }
    public String getClientType(){return clientType;} public String getSessionScope(){return sessionScope;}
    public Instant getOverallExpiresAt(){return overallExpiresAt;} public Instant getIdleExpiresAt(){return idleExpiresAt;}
    public Instant getLastActivityAt(){return lastActivityAt;} public Instant getReauthenticatedAt(){return reauthenticatedAt;}
    public Instant getMfaAuthenticatedAt(){return mfaAuthenticatedAt;}
    public User getUser() {
        return user;
    }

    public UUID getId() {
        return id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }
}
