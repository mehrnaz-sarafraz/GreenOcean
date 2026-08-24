package com.greenocean.backend.profile.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Pattern(regexp = "^[A-Za-z0-9_]{3,30}$") String username,
        @Size(min = 1, max = 80) String displayName,
        @Size(max = 500) String bio,
        @Size(max = 2048) String avatarUrl,
        @Pattern(regexp = "^$|^[A-Za-z]{2}$") String countryCode,
        @Size(max = 100) String city,
        @Min(1900) @Max(2100) Short birthYear,
        Boolean profilePrivate,
        Boolean showLocation,
        Boolean showBirthYear
) {
}
