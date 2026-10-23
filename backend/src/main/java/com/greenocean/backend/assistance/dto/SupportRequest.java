package com.greenocean.backend.assistance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupportRequest(
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(min = 10, max = 5000) String message
) {}
