package com.greenocean.backend.post.controller;

import com.greenocean.backend.post.service.PostService;
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
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final PostService postService;

    public CommentController(PostService postService) { this.postService = postService; }

    @PutMapping("/{commentId}/like")
    public ResponseEntity<Void> like(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID commentId) {
        postService.setCommentLike(commentId, UUID.fromString(jwt.getSubject()), true);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<Void> unlike(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID commentId) {
        postService.setCommentLike(commentId, UUID.fromString(jwt.getSubject()), false);
        return ResponseEntity.noContent().build();
    }
}
