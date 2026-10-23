package com.greenocean.backend.moderation.dto;

import java.time.Instant;
import java.util.UUID;

public record AppealResponse(
        UUID id, UUID reportId, String reason, String status, String decisionNote,
        Instant createdAt, Instant decidedAt
) {}
