package com.greenocean.backend.assistance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Size(max = 500) String token,
        @NotBlank @Size(min = 15, max = 256) String newPassword
) {}
