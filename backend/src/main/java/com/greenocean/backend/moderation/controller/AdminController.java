package com.greenocean.backend.moderation.controller;

import com.greenocean.backend.moderation.dto.AdminDashboardResponse;
import com.greenocean.backend.moderation.dto.ModerationActionRequest;
import com.greenocean.backend.moderation.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
public class AdminController {
    private final AdminService service;
    public AdminController(AdminService service) { this.service = service; }

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() { return service.dashboard(); }

    @PatchMapping("/reports/{id}")
    public ResponseEntity<Void> report(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                       @Valid @RequestBody ModerationActionRequest request) {
        service.report(id, userId(jwt), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/verifications/{id}")
    public ResponseEntity<Void> verification(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                             @Valid @RequestBody ModerationActionRequest request) {
        service.verification(id, userId(jwt), request);
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
