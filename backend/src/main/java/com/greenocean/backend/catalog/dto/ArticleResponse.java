package com.greenocean.backend.catalog.dto;

import tools.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

public record ArticleResponse(
        UUID id, UUID authorId, String title, String summary, String topic, String readTime, String status,
        boolean pinned, String evidenceLevel, JsonNode sections, JsonNode takeaways, JsonNode references,
        long helpfulCount, boolean helpful, boolean saved, Instant publishedAt
) {
}
