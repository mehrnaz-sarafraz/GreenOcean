package com.greenocean.backend.professional.dto;

import java.time.Instant;
import java.util.UUID;

public record ProfessionalDocumentResponse(
        UUID id, String originalName, String contentType, long byteSize, String sha256,
        String scanStatus, Instant uploadedAt
) {
}
