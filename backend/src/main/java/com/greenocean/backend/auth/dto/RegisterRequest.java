package com.greenocean.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Pattern(regexp = com.greenocean.backend.auth.service.EmailIdentity.PATTERN, message = "Enter a valid email address using an ASCII mailbox and domain") @Size(max = 320) String email,
        @NotBlank @Size(max = 256) String password,
        @Pattern(regexp = "^[A-Za-z0-9_]{3,30}$") String username,
        @Size(max = 80) String displayName,
        @Min(1900) @Max(2100) Short birthYear,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") String countryCode,
        @Size(max = 100) String city,
        @Pattern(regexp = "MEMBER") String intendedRole,
        @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.AssertTrue Boolean adultSelfAttested,
        @jakarta.validation.constraints.NotNull @Min(1) Integer termsVersion
) {
    public RegisterRequest {
        email = com.greenocean.backend.auth.service.EmailIdentity.normalize(email);
        countryCode = countryCode == null ? null : countryCode.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
