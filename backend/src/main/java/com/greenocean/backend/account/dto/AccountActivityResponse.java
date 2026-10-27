package com.greenocean.backend.account.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AccountActivityResponse(
        List<PostItem> posts,
        List<CommentItem> comments,
        List<CommunityMembership> communities,
        List<SavedItem> savedItems,
        List<ListenerSession> listenerSessions,
        List<ListenerMessage> listenerMessages
) {
    public record PostItem(UUID id, String body, String category, String postType, String visibility, Instant createdAt) {}
    public record CommentItem(UUID id, UUID postId, String body, Instant createdAt) {}
    public record CommunityMembership(UUID id, String name, String slug, String role, Instant joinedAt) {}
    public record ListenerSession(UUID id,String status,String reasonCode,String participantRole,Instant createdAt,Instant endedAt) {}
    public record ListenerMessage(UUID id,UUID conversationId,String body,Instant createdAt) {}
    public record SavedItem(String type, UUID id, String title, Instant savedAt) {}
}
