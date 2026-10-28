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
        Long memberCount,
        boolean member,
        String membershipRole,
        boolean joinRequested,
        Instant createdAt,
        String privacyType,
        String status,
        long rulesVersion,
        String rulesJson,Long joinRequestRevision
) {
}
