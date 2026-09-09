package com.greenocean.backend.moderation.dto;

import java.time.Instant;
import java.util.UUID;

public record ReportResponse(UUID id, String targetType, String reason, String status, Instant createdAt) {}
