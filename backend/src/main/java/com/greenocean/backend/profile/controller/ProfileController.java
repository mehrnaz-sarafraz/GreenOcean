package com.greenocean.backend.profile.controller;

import com.greenocean.backend.profile.dto.OwnProfileResponse;
import com.greenocean.backend.profile.dto.PublicProfileResponse;
import com.greenocean.backend.profile.dto.UpdateProfileRequest;
import com.greenocean.backend.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import com.greenocean.backend.common.api.PageResponse;
import com.greenocean.backend.post.dto.PostResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public OwnProfileResponse getOwnProfile(@AuthenticationPrincipal Jwt jwt) {
        return profileService.getOwnProfile(UUID.fromString(jwt.getSubject()));
    }

    @PatchMapping("/me")
    public OwnProfileResponse updateOwnProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return profileService.updateOwnProfile(UUID.fromString(jwt.getSubject()), request);
    }

    @GetMapping("/{username}")
    public PublicProfileResponse getPublicProfile(@AuthenticationPrincipal Jwt jwt, @PathVariable String username) {
        return profileService.getPublicProfile(UUID.fromString(jwt.getSubject()), username);
    }

    @GetMapping("/{username}/posts")
    public PageResponse<PostResponse> getPublicPosts(@AuthenticationPrincipal Jwt jwt, @PathVariable String username,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        if (page < 0) throw new IllegalArgumentException("page cannot be negative");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
        return profileService.getPublicPosts(UUID.fromString(jwt.getSubject()), username, page, size);
    }
}
