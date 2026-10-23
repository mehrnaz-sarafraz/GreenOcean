package com.greenocean.backend.social.service;

import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;
import com.greenocean.backend.social.dto.SocialConnectionResponse;
import com.greenocean.backend.social.dto.FollowActionResponse;
import com.greenocean.backend.social.dto.FollowRequestResponse;

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
    public FollowActionResponse follow(UUID currentUserId, UUID targetUserId) {
        validateTarget(currentUserId, targetUserId);
        if (interactions.hasBlockBetween(currentUserId, targetUserId)) {
            throw new ConflictException("Follow is not available between blocked accounts");
        }
        if (interactions.isFollowing(currentUserId, targetUserId)) {
            return new FollowActionResponse("FOLLOWING");
        }
        if (interactions.isProfilePrivate(targetUserId)) {
            if (interactions.requestFollow(currentUserId, targetUserId)) {
                notificationService.followRequested(currentUserId, targetUserId);
            }
            return new FollowActionResponse("REQUESTED");
        }
        if (interactions.follow(currentUserId, targetUserId)) {
            notificationService.followed(currentUserId, targetUserId);
        }
        return new FollowActionResponse("FOLLOWING");
    }

    @Transactional
    public void unfollow(UUID currentUserId, UUID targetUserId) {
        interactions.unfollow(currentUserId, targetUserId);
    }

    @Transactional
    public void acceptFollowRequest(UUID currentUserId, UUID requesterId) {
        validateTarget(currentUserId, requesterId);
        if (!interactions.acceptFollowRequest(requesterId, currentUserId)) {
            throw new NotFoundException("Pending follow request was not found");
        }
        notificationService.followed(currentUserId, requesterId);
    }

    @Transactional
    public void declineFollowRequest(UUID currentUserId, UUID requesterId) {
        validateTarget(currentUserId, requesterId);
        if (!interactions.declineFollowRequest(requesterId, currentUserId)) {
            throw new NotFoundException("Pending follow request was not found");
        }
    }

    @Transactional
    public void cancelFollowRequest(UUID currentUserId, UUID targetId) {
        interactions.cancelFollowRequest(currentUserId, targetId);
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

    @Transactional(readOnly = true)
    public List<SocialConnectionResponse> following(UUID userId) { return interactions.following(userId); }

    @Transactional(readOnly = true)
    public List<SocialConnectionResponse> followers(UUID userId) { return interactions.followers(userId); }

    @Transactional(readOnly = true)
    public List<SocialConnectionResponse> blocked(UUID userId) { return interactions.blocked(userId); }

    @Transactional(readOnly = true)
    public List<FollowRequestResponse> incomingRequests(UUID userId) { return interactions.incomingFollowRequests(userId); }

    @Transactional(readOnly = true)
    public List<FollowRequestResponse> outgoingRequests(UUID userId) { return interactions.outgoingFollowRequests(userId); }

    private void validateTarget(UUID currentUserId, UUID targetUserId) {
        if (currentUserId.equals(targetUserId)) throw new IllegalArgumentException("You cannot target your own account");
        if (!userRepository.existsById(targetUserId)) throw new NotFoundException("User was not found");
    }
}
