package com.greenocean.backend.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        String name,
        String subtitle,
        String lastMessage,
        Instant lastMessageAt,
        long unread,
        boolean verified,
        String kind,
        boolean online
) {
}
