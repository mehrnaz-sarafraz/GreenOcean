package com.greenocean.backend.search.service;

import java.util.UUID;

public record UserSearchResponse(
        UUID userId,
        String username,
        String displayName,
        String avatarUrl,
        String bio,
        boolean profilePrivate,
        boolean following,
        long followerCount
) {
}
