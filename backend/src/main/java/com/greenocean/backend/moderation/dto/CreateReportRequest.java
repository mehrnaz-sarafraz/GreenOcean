package com.greenocean.backend.moderation.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReportRequest(
        UUID targetUserId,
        UUID postId,
        UUID commentId,
        UUID messageId,
        @NotBlank @Pattern(regexp = "HARASSMENT|SPAM|HATE_SPEECH|SEXUAL_CONTENT|SELF_HARM_CONTENT|MISINFORMATION|OTHER") String reason,
        @Size(max = 1000) String description
) {
    @AssertTrue(message = "Exactly one report target is required")
    public boolean isSingleTarget() {
        return java.util.stream.Stream.of(targetUserId, postId, commentId, messageId).filter(java.util.Objects::nonNull).count() == 1;
    }
}
