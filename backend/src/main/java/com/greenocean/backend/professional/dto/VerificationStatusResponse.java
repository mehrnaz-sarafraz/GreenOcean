package com.greenocean.backend.professional.dto;

import java.time.Instant;
import java.util.UUID;

public record VerificationStatusResponse(
        UUID id,
        String status,
        String profession,
        String country,
        Instant submittedAt,
        Instant reviewedAt,
        String reviewerNote,
        String informationRequest
) {}
