package com.greenocean.backend.post.dto;

import com.greenocean.backend.post.entity.PostVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreatePostRequest(
        @NotBlank @Size(max = 10000) String body,
        boolean anonymous,
        @NotNull PostVisibility visibility,
        UUID communityId,
        @Size(max = 120) String contentWarning
) {
}
