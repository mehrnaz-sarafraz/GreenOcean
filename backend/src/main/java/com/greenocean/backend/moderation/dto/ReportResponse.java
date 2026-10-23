package com.greenocean.backend.moderation.dto;

import java.time.Instant;
import java.util.UUID;

public record ReportResponse(
        UUID id, String targetType, UUID targetId, String reason, String description,
        String severity, String status, Instant createdAt, Instant resolvedAt, String resolutionNote
) {}
