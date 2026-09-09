package com.greenocean.backend.auth.service;

import java.time.Instant;

/**
 * Boundary for the transactional auth domain. A production deployment must
 * provide a delivery adapter (SMTP/provider) and enable the production guard.
 */
public interface AuthTokenDelivery {
    void sendEmailVerification(String email, String token, Instant expiresAt);

    void sendPasswordReset(String email, String token, Instant expiresAt);
}
