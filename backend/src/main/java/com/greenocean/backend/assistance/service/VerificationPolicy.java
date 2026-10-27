package com.greenocean.backend.assistance.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public record VerificationPolicy(int attempts, int deliveries, int correctionAttempts, Duration window, int cooldownSeconds) {
    public VerificationPolicy(
        @Value("${greenocean.verification.attempts:5}") int attempts,
        @Value("${greenocean.verification.deliveries:5}") int deliveries,
        @Value("${greenocean.verification.correction-attempts:3}") int correctionAttempts,
        @Value("${greenocean.verification.window:PT15M}") Duration window,
        @Value("${greenocean.verification.resend-cooldown-seconds:60}") int cooldownSeconds) {
        if(attempts<1||deliveries<1||correctionAttempts<1||window.isNegative()||window.isZero()||cooldownSeconds<0)
            throw new IllegalArgumentException("Verification limits must be positive");
        this.attempts=attempts;this.deliveries=deliveries;this.correctionAttempts=correctionAttempts;this.window=window;this.cooldownSeconds=cooldownSeconds;
    }
}
