package com.greenocean.backend.professional.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProfessionalReviewRequest(
        @Min(1) @Max(5) int rating,
        @NotBlank @Size(min = 20, max = 2000) String body
) {}
