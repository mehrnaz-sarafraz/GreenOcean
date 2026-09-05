package com.greenocean.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendEmailVerificationRequest(

        @NotBlank
        @Email
        String email

) {
}