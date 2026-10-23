package com.greenocean.backend.post.dto;

import com.greenocean.backend.post.entity.PostVisibility;
import com.greenocean.backend.post.entity.PostType;

import java.time.Instant;
import java.util.UUID;

public record PostResponse(
        UUID id,
        AuthorSummary author,
        UUID communityId,
        String body,
        boolean anonymous,
        PostVisibility visibility,
        String contentWarning,
        long likeCount,
        long commentCount,
        boolean liked,
        boolean bookmarked,
        SupportCategoryResponse category,
        PostType postType,
        String mood,
        ProfessionalReplyResponse professionalReply,
        Instant createdAt
) {
}
