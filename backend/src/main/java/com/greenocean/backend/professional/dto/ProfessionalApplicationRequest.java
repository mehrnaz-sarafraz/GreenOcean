package com.greenocean.backend.professional.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProfessionalApplicationRequest(
        @NotBlank @Size(max = 30) String professionalType,
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 200) String specialization,
        @NotBlank @Size(max = 4000) String bio,
        @NotNull @Min(0) @Max(80) Integer yearsOfExperience,
        @Size(max = 20) List<@Size(max = 100) String> specialties,
        @Size(max = 20) List<@Size(max = 80) String> languages,
        @Size(max = 40) String gender,
        @NotBlank @Size(max = 100) String country,
        @Size(max = 100) String city,
        @Size(max = 180) String workplace,
        @Size(max = 180) String clinicName,
        @Size(max = 300) String clinicAddress,
        @Size(max = 20) List<@Size(max = 240) String> education,
        @NotBlank @Size(max = 100) String licenseNumber,
        @Size(max = 10) List<@Size(max = 80) String> consultationModes,
        @Size(min = 1, max = 20) List<@Size(max = 240) String> documentNames
) {}
