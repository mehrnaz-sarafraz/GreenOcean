package com.greenocean.backend.professional.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfessionalApplicationResponse(
        UUID id, String profession, String country, List<String> documentNames,
        List<ProfessionalDocumentResponse> documents,
        String status, String note, Instant submittedAt, Instant reviewedAt
) {}
