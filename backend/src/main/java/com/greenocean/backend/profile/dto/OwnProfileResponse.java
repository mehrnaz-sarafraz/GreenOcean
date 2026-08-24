package com.greenocean.backend.profile.dto;

import java.time.Instant;
import java.util.UUID;

public record OwnProfileResponse(
        UUID userId,
        String username,
        String displayName,
        String bio,
        String avatarUrl,
        String countryCode,
        String city,
        Short birthYear,
        boolean profilePrivate,
        boolean showLocation,
        boolean showBirthYear,
        Instant createdAt,
        Instant updatedAt
) {
}
