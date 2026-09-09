package com.greenocean.backend.moderation.controller;

import com.greenocean.backend.moderation.dto.CreateReportRequest;
import com.greenocean.backend.moderation.dto.ReportResponse;
import com.greenocean.backend.moderation.service.SafetyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/safety")
public class SafetyController {
    private final SafetyService service;
    public SafetyController(SafetyService service) { this.service = service; }

    @PostMapping("/reports")
    public ResponseEntity<ReportResponse> report(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateReportRequest request) {
        return ResponseEntity.status(201).body(service.report(userId(jwt), request));
    }

    @GetMapping("/reports/mine")
    public List<ReportResponse> own(@AuthenticationPrincipal Jwt jwt) { return service.own(userId(jwt)); }

    @PutMapping("/blocks/{targetId}")
    public ResponseEntity<Void> block(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID targetId) {
        service.block(userId(jwt), targetId); return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/blocks/{targetId}")
    public ResponseEntity<Void> unblock(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID targetId) {
        service.unblock(userId(jwt), targetId); return ResponseEntity.noContent().build();
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
