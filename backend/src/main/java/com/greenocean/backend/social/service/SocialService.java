package com.greenocean.backend.social.service;

import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SocialService {
    private final UserRepository userRepository;
    private final SocialInteractionRepository interactions;
    private final NotificationService notificationService;

    public SocialService(UserRepository userRepository, SocialInteractionRepository interactions,
                         NotificationService notificationService) {
        this.userRepository = userRepository;
        this.interactions = interactions;
        this.notificationService = notificationService;
    }

    @Transactional
    public void follow(UUID currentUserId, UUID targetUserId) {
        validateTarget(currentUserId, targetUserId);
        if (interactions.hasBlockBetween(currentUserId, targetUserId)) {
            throw new ConflictException("Follow is not available between blocked accounts");
        }
        if (interactions.follow(currentUserId, targetUserId)) {
            notificationService.followed(currentUserId, targetUserId);
        }
    }

    @Transactional
    public void unfollow(UUID currentUserId, UUID targetUserId) {
        interactions.unfollow(currentUserId, targetUserId);
    }

    @Transactional
    public void block(UUID currentUserId, UUID targetUserId) {
        validateTarget(currentUserId, targetUserId);
        interactions.block(currentUserId, targetUserId);
    }

    @Transactional
    public void unblock(UUID currentUserId, UUID targetUserId) {
        interactions.unblock(currentUserId, targetUserId);
    }

    private void validateTarget(UUID currentUserId, UUID targetUserId) {
        if (currentUserId.equals(targetUserId)) throw new IllegalArgumentException("You cannot target your own account");
        if (!userRepository.existsById(targetUserId)) throw new NotFoundException("User was not found");
    }
}
