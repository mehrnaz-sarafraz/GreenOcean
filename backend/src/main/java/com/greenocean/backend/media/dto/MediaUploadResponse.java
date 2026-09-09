package com.greenocean.backend.media.dto;

import java.time.Instant;
import java.util.UUID;

public record MediaUploadResponse(
        UUID id,
        String filename,
        String contentType,
        long sizeBytes,
        String purpose,
        String status,
        Instant expiresAt
) {}
