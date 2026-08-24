package com.greenocean.backend.post.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID postId,
        UUID parentCommentId,
        AuthorSummary author,
        String body,
        boolean anonymous,
        long likeCount,
        boolean liked,
        Instant createdAt
) {
}
