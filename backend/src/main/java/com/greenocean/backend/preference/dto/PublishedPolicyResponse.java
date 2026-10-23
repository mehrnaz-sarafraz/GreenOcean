package com.greenocean.backend.preference.dto;

import java.time.Instant;

public record PublishedPolicyResponse(
        String key,
        int version,
        String title,
        String summary,
        String content,
        Instant publishedAt
) {
}
