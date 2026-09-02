package com.greenocean.backend.post.controller;

import com.greenocean.backend.common.api.PageResponse;
import com.greenocean.backend.post.dto.CommentResponse;
import com.greenocean.backend.post.dto.CreateCommentRequest;
import com.greenocean.backend.post.dto.CreatePostRequest;
import com.greenocean.backend.post.dto.PostResponse;
import com.greenocean.backend.post.service.PostService;
import com.greenocean.backend.post.entity.FeedMode;
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
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(@AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(userId(jwt), request));
    }

    @GetMapping("/feed")
    public PageResponse<PostResponse> feed(@AuthenticationPrincipal Jwt jwt,
                                           @RequestParam(defaultValue = "FOR_YOU") FeedMode mode,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return postService.feed(userId(jwt), mode, validPage(page), validSize(size));
    }

    @GetMapping("/{postId}")
    public PostResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId) {
        return postService.get(postId, userId(jwt));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId) {
        postService.delete(postId, userId(jwt));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> comment(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId,
                                                    @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.comment(postId, userId(jwt), request));
    }

    @GetMapping("/{postId}/comments")
    public PageResponse<CommentResponse> comments(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "50") int size) {
        return postService.comments(postId, userId(jwt), validPage(page), validSize(size));
    }

    @PutMapping("/{postId}/like")
    public ResponseEntity<Void> like(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId) {
        postService.setPostLike(postId, userId(jwt), true);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Void> unlike(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId) {
        postService.setPostLike(postId, userId(jwt), false);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{postId}/bookmark")
    public ResponseEntity<Void> bookmark(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId) {
        postService.setBookmark(postId, userId(jwt), true);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}/bookmark")
    public ResponseEntity<Void> removeBookmark(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId) {
        postService.setBookmark(postId, userId(jwt), false);
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
    private int validPage(int page) { if (page < 0) throw new IllegalArgumentException("page cannot be negative"); return page; }
    private int validSize(int size) { if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100"); return size; }
}
