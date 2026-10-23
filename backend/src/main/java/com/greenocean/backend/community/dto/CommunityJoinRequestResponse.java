package com.greenocean.backend.community.dto;

import java.time.Instant;
import java.util.UUID;

public record CommunityJoinRequestResponse(
        UUID userId, String displayName, String username, String avatarUrl, Instant requestedAt
) {
}
