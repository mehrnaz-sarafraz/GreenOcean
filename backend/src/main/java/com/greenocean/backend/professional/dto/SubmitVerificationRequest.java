package com.greenocean.backend.professional.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record SubmitVerificationRequest(
        @NotBlank @Pattern(regexp = "PSYCHOLOGIST|PSYCHIATRIST|COUNSELOR|THERAPIST|OTHER") String professionalType,
        @NotBlank @Size(max = 100) String profession,
        @NotBlank @Size(max = 100) String country,
        @NotBlank @Size(max = 100) String licenseNumber,
        @Size(max = 200) String specialization,
        @NotEmpty @Size(max = 8) List<UUID> documentIds
) {}
