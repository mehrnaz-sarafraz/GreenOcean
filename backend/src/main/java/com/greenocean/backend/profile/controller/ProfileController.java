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
    public PublicProfileResponse getPublicProfile(@PathVariable String username) {
        return profileService.getPublicProfile(username);
    }
}
