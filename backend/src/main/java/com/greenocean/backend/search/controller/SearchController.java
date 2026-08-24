package com.greenocean.backend.search.controller;

import com.greenocean.backend.common.api.PageResponse;
import com.greenocean.backend.post.dto.PostResponse;
import com.greenocean.backend.search.service.SearchService;
import com.greenocean.backend.search.service.UserSearchResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) { this.searchService = searchService; }

    @GetMapping("/users")
    public PageResponse<UserSearchResponse> users(@AuthenticationPrincipal Jwt jwt, @RequestParam String q,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        validate(q, page, size); return searchService.users(userId(jwt), q, page, size);
    }

    @GetMapping("/posts")
    public PageResponse<PostResponse> posts(@AuthenticationPrincipal Jwt jwt, @RequestParam String q,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        validate(q, page, size); return searchService.posts(userId(jwt), q, page, size);
    }

    private void validate(String query, int page, int size) {
        if (query == null || query.trim().length() < 2 || query.length() > 100) throw new IllegalArgumentException("q must contain 2 to 100 characters");
        if (page < 0) throw new IllegalArgumentException("page cannot be negative");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
