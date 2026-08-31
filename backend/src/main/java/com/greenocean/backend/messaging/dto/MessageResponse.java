package com.greenocean.backend.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID senderId,
        String senderName,
        String body,
        Instant createdAt,
        boolean mine,
        boolean system
) {
}
