package com.greenocean.backend.messaging.dto;

import java.util.UUID;

public record SupportChannelResponse(
        UUID id,
        UUID conversationId,
        String name,
        String slug,
        String description,
        String category,
        String icon,
        long memberCount,
        long onlineCount,
        boolean joined,
        String type,
        boolean moderated,
        String nextEvent
) {
}
