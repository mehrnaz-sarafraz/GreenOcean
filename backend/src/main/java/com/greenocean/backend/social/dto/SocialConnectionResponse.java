package com.greenocean.backend.social.dto;

import java.time.Instant;
import java.util.UUID;

public record SocialConnectionResponse(
        UUID userId,
        String displayName,
        String username,
        String avatarUrl,
        Instant connectedAt
) {
}
