package com.greenocean.backend.notification.service;

import com.greenocean.backend.common.api.PageResponse;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.notification.dto.NotificationResponse;
import com.greenocean.backend.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public void postLiked(UUID actorId, UUID postId) {
        repository.postAuthor(postId).ifPresent(recipient -> create(recipient, actorId, actorId, "LIKE", postId, null));
    }

    public void followed(UUID actorId, UUID followedUserId) {
        create(followedUserId, actorId, actorId, "FOLLOW", null, null);
    }

    public void commentLiked(UUID actorId, UUID commentId) {
        repository.commentAuthor(commentId).ifPresent(recipient ->
                create(recipient, actorId, actorId, "LIKE", repository.commentPost(commentId).orElse(null), commentId));
    }

    public void commentCreated(UUID actorId, boolean anonymous, UUID postId, UUID commentId, UUID parentCommentId) {
        UUID visibleActorId = anonymous ? null : actorId;
        if (parentCommentId == null) {
            repository.postAuthor(postId)
                    .ifPresent(recipient -> create(recipient, actorId, visibleActorId, "COMMENT", postId, commentId));
        } else {
            repository.commentAuthor(parentCommentId)
                    .ifPresent(recipient -> create(recipient, actorId, visibleActorId, "REPLY", postId, commentId));
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(UUID userId, int page, int size) {
        List<NotificationResponse> results = repository.findAll(userId, page, size);
        boolean hasNext = results.size() > size;
        return new PageResponse<>(hasNext ? List.copyOf(results.subList(0, size)) : List.copyOf(results),
                page, size, hasNext);
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID userId) {
        return repository.unreadCount(userId);
    }

    @Transactional
    public void markRead(UUID notificationId, UUID userId) {
        if (repository.markRead(notificationId, userId) == 0) {
            throw new NotFoundException("Notification was not found");
        }
    }

    @Transactional
    public void markAllRead(UUID userId) {
        repository.markAllRead(userId);
    }

    private void create(UUID recipientId, UUID actingUserId, UUID visibleActorId,
                        String type, UUID postId, UUID commentId) {
        if (!recipientId.equals(actingUserId)) {
            repository.create(recipientId, visibleActorId, type, postId, commentId);
        }
    }
}
