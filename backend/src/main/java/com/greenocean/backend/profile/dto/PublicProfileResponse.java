package com.greenocean.backend.profile.dto;

import java.util.UUID;
import java.time.Instant;

public record PublicProfileResponse(
        UUID userId,
        String username,
        String displayName,
        String bio,
        String avatarUrl,
        String countryCode,
        String city,
        Short birthYear,
        boolean profilePrivate,
        boolean following,
        boolean followRequested,
        boolean canSeeActivity,
        long followerCount,
        long followingCount,
        long postCount,
        long helpfulReactionCount,
        Instant memberSince
) {
}
