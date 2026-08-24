package com.greenocean.backend.profile.dto;

import java.util.UUID;

public record PublicProfileResponse(
        UUID userId,
        String username,
        String displayName,
        String bio,
        String avatarUrl,
        String countryCode,
        String city,
        Short birthYear,
        boolean profilePrivate
) {
}
