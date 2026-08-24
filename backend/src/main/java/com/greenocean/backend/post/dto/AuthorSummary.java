package com.greenocean.backend.post.dto;

import java.util.UUID;

public record AuthorSummary(UUID userId, String username, String displayName, String avatarUrl) {
}
