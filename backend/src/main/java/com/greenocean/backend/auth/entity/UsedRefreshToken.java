package com.greenocean.backend.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "used_refresh_tokens")
public class UsedRefreshToken {
    @Id
    @Column(name = "token_hash", length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private UserSession session;

    @Column(name = "used_at", nullable = false)
    private Instant usedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected UsedRefreshToken() {
    }

    public UsedRefreshToken(String tokenHash, UserSession session, Instant usedAt, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.session = session;
        this.usedAt = usedAt;
        this.expiresAt = expiresAt;
    }

    public UserSession getSession() {
        return session;
    }
}
