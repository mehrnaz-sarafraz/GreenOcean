package com.greenocean.backend.social.controller;

import com.greenocean.backend.social.service.SocialService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social")
public class SocialController {
    private final SocialService socialService;

    public SocialController(SocialService socialService) { this.socialService = socialService; }

    @PutMapping("/follows/{targetUserId}")
    public ResponseEntity<Void> follow(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID targetUserId) {
        socialService.follow(userId(jwt), targetUserId); return ResponseEntity.noContent().build();
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

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
