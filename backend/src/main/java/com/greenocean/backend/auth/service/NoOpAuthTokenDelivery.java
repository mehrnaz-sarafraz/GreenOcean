package com.greenocean.backend.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class NoOpAuthTokenDelivery implements AuthTokenDelivery {
    private static final Logger log = LoggerFactory.getLogger(NoOpAuthTokenDelivery.class);

    @Override
    public void sendEmailVerification(String email, String token, Instant expiresAt) {
        log.warn("Email verification delivery is not configured; token was issued but is not logged");
    }

    @Override
    public void sendPasswordReset(String email, String token, Instant expiresAt) {
        log.warn("Password reset delivery is not configured; token was issued but is not logged");
    }
}
