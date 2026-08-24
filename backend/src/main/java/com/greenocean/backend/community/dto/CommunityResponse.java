package com.greenocean.backend.community.dto;

import java.time.Instant;
import java.util.UUID;

public record CommunityResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String iconUrl,
        boolean privateCommunity,
        long memberCount,
        boolean member,
        String membershipRole,
        Instant createdAt
) {
}
