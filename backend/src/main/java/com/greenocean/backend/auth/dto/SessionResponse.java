package com.greenocean.backend.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        String deviceName,
        Instant createdAt,
        Instant lastUsedAt,
        Instant expiresAt,
        boolean current
) {
}
