package com.greenocean.backend.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ModerationActionRequest(
        @NotBlank @Pattern(regexp = "^(APPROVE|REVIEW|REMOVE|REQUEST_INFO|APPROVE_VERIFICATION|REJECT_VERIFICATION)$") String action,
        @Size(max = 1000) String note
) {
}
