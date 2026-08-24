package com.greenocean.backend.community.controller;

import com.greenocean.backend.common.api.PageResponse;
import com.greenocean.backend.community.dto.CommunityResponse;
import com.greenocean.backend.community.dto.CreateCommunityRequest;
import com.greenocean.backend.community.service.CommunityService;
import com.greenocean.backend.post.dto.PostResponse;
import com.greenocean.backend.post.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/communities")
public class CommunityController {
    private final CommunityService communityService;
    private final PostService postService;

    public CommunityController(CommunityService communityService, PostService postService) {
        this.communityService = communityService;
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<CommunityResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                     @Valid @RequestBody CreateCommunityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(communityService.create(userId(jwt), request));
    }

    @GetMapping
    public PageResponse<CommunityResponse> discover(@AuthenticationPrincipal Jwt jwt,
                                                     @RequestParam(defaultValue = "") String q,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        validatePage(page, size);
        if (q.length() > 100) throw new IllegalArgumentException("q cannot exceed 100 characters");
        return communityService.discover(userId(jwt), q, page, size);
    }

    @GetMapping("/slug/{slug}")
    public CommunityResponse getBySlug(@AuthenticationPrincipal Jwt jwt, @PathVariable String slug) {
        return communityService.getBySlug(slug, userId(jwt));
    }

    @PutMapping("/{communityId}/membership")
    public CommunityResponse join(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID communityId) {
        return communityService.join(communityId, userId(jwt));
    }

    @DeleteMapping("/{communityId}/membership")
    public ResponseEntity<Void> leave(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID communityId) {
        communityService.leave(communityId, userId(jwt));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{communityId}/posts")
    public PageResponse<PostResponse> posts(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID communityId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        validatePage(page, size);
        return postService.communityFeed(communityId, userId(jwt), page, size);
    }

    private void validatePage(int page, int size) {
        if (page < 0) throw new IllegalArgumentException("page cannot be negative");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
