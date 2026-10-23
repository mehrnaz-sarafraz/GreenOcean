package com.greenocean.backend.preference.controller;

import com.greenocean.backend.preference.dto.UpdateUserPreferencesRequest;
import com.greenocean.backend.preference.dto.UserPreferencesResponse;
import com.greenocean.backend.preference.service.UserPreferencesService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/preferences/me")
public class UserPreferencesController {
    private final UserPreferencesService service;
    public UserPreferencesController(UserPreferencesService service) { this.service = service; }

    @GetMapping
    public UserPreferencesResponse get(@AuthenticationPrincipal Jwt jwt) { return service.get(userId(jwt)); }

    @PatchMapping
    public UserPreferencesResponse update(@AuthenticationPrincipal Jwt jwt,
                                           @Valid @RequestBody UpdateUserPreferencesRequest request) {
        return service.update(userId(jwt), request);
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
