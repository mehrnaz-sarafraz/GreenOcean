package com.greenocean.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Size(max = 500) String token,
        @NotBlank @Size(min = 12, max = 72) String newPassword
) {
}
