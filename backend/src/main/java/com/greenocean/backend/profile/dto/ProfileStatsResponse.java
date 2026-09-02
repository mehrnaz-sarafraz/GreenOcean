package com.greenocean.backend.profile.dto;

public record ProfileStatsResponse(
        long followers,
        long following,
        long stories,
        long helpfulReactions,
        long savedPosts,
        long blockedAccounts
) {
}
