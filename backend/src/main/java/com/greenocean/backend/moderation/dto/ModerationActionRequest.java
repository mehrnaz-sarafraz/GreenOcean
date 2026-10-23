package com.greenocean.backend.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ModerationActionRequest(
        @NotBlank @Pattern(regexp = "^(APPROVE|REVIEW|REMOVE|REQUEST_INFO|APPROVE_VERIFICATION|REJECT_VERIFICATION|APPROVE_REVIEW|REJECT_REVIEW|APPROVE_LISTENER|REJECT_LISTENER|REVIEW_LISTENER|START_CAMPAIGN|PAUSE_CAMPAIGN|COMPLETE_CAMPAIGN|REVIEW_APPEAL|UPHOLD_APPEAL|OVERTURN_APPEAL)$") String action,
        @Size(max = 1000) String note
) {
}
