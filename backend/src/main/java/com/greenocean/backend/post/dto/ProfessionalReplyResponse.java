package com.greenocean.backend.post.dto;

import java.time.Instant;
import java.util.UUID;

public record ProfessionalReplyResponse(
        UUID id,
        ProfessionalAuthor professional,
        String body,
        long helpfulCount,
        Instant createdAt
) {
    public record ProfessionalAuthor(UUID userId, String username, String displayName, String title, String avatarUrl) {
    }
}
