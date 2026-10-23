package com.greenocean.backend.social.controller;

import com.greenocean.backend.social.service.SocialService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;
import com.greenocean.backend.social.dto.SocialConnectionResponse;
import com.greenocean.backend.social.dto.FollowActionResponse;
import com.greenocean.backend.social.dto.FollowRequestResponse;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/v1/social")
public class SocialController {
    private final SocialService socialService;

    public SocialController(SocialService socialService) { this.socialService = socialService; }

    @PutMapping("/follows/{targetUserId}")
    public FollowActionResponse follow(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID targetUserId) {
        return socialService.follow(userId(jwt), targetUserId);
    }

    @DeleteMapping("/follows/{targetUserId}")
    public ResponseEntity<Void> unfollow(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID targetUserId) {
        socialService.unfollow(userId(jwt), targetUserId); return ResponseEntity.noContent().build();
    }

    @PutMapping("/blocks/{targetUserId}")
    public ResponseEntity<Void> block(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID targetUserId) {
        socialService.block(userId(jwt), targetUserId); return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/blocks/{targetUserId}")
    public ResponseEntity<Void> unblock(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID targetUserId) {
        socialService.unblock(userId(jwt), targetUserId); return ResponseEntity.noContent().build();
    }

    @GetMapping("/following")
    public List<SocialConnectionResponse> following(@AuthenticationPrincipal Jwt jwt) {
        return socialService.following(userId(jwt));
    }

    @GetMapping("/followers")
    public List<SocialConnectionResponse> followers(@AuthenticationPrincipal Jwt jwt) {
        return socialService.followers(userId(jwt));
    }

    @GetMapping("/blocks")
    public List<SocialConnectionResponse> blocked(@AuthenticationPrincipal Jwt jwt) {
        return socialService.blocked(userId(jwt));
    }

    @GetMapping("/follow-requests/incoming")
    public List<FollowRequestResponse> incomingRequests(@AuthenticationPrincipal Jwt jwt) {
        return socialService.incomingRequests(userId(jwt));
    }

    @GetMapping("/follow-requests/outgoing")
    public List<FollowRequestResponse> outgoingRequests(@AuthenticationPrincipal Jwt jwt) {
        return socialService.outgoingRequests(userId(jwt));
    }

    @PostMapping("/follow-requests/{requesterId}/accept")
    public ResponseEntity<Void> acceptRequest(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID requesterId) {
        socialService.acceptFollowRequest(userId(jwt), requesterId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/follow-requests/{requesterId}")
    public ResponseEntity<Void> declineRequest(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID requesterId) {
        socialService.declineFollowRequest(userId(jwt), requesterId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/follow-requests/outgoing/{targetId}")
    public ResponseEntity<Void> cancelRequest(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID targetId) {
        socialService.cancelFollowRequest(userId(jwt), targetId);
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
