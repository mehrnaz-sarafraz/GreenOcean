package com.greenocean.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank @Size(max = 256) String currentPassword,
        @NotBlank @Size(min = 15, max = 256) String newPassword
) {
}
