package com.greenocean.backend.moderation.controller;

import com.greenocean.backend.moderation.dto.AdminDashboardResponse;
import com.greenocean.backend.moderation.dto.AdminOperationsResponse;
import com.greenocean.backend.moderation.dto.ModerationActionRequest;
import com.greenocean.backend.moderation.dto.BroadcastRequest;
import com.greenocean.backend.moderation.dto.AdminExportFile;
import com.greenocean.backend.moderation.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
public class AdminController {
    private final AdminService service;
    public AdminController(AdminService service) { this.service = service; }

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() { return service.dashboard(); }

    @GetMapping("/operations")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminOperationsResponse operations() { return service.operations(); }

    @PatchMapping("/reports/{id}")
    public ResponseEntity<Void> report(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                       @Valid @RequestBody ModerationActionRequest request) {
        service.report(id, userId(jwt), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/verifications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> verification(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                             @Valid @RequestBody ModerationActionRequest request) {
        service.verification(id, userId(jwt), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reviews/{id}")
    public ResponseEntity<Void> review(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                       @Valid @RequestBody ModerationActionRequest request) {
        service.review(id, userId(jwt), request); return ResponseEntity.noContent().build();
    }

    @PatchMapping("/listener-applications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> listenerApplication(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                                     @Valid @RequestBody ModerationActionRequest request) {
        service.listenerApplication(id, userId(jwt), request); return ResponseEntity.noContent().build();
    }

    @PatchMapping("/campaigns/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> campaign(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                         @Valid @RequestBody ModerationActionRequest request) {
        service.campaign(id, userId(jwt), request); return ResponseEntity.noContent().build();
    }

    @PatchMapping("/appeals/{id}")
    public ResponseEntity<Void> appeal(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                       @Valid @RequestBody ModerationActionRequest request) {
        service.appeal(id, userId(jwt), request); return ResponseEntity.noContent().build();
    }

    @PostMapping("/broadcasts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, UUID>> broadcast(@AuthenticationPrincipal Jwt jwt,
                                                        @Valid @RequestBody BroadcastRequest request) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(Map.of("id", service.broadcast(userId(jwt), request)));
    }

    @PostMapping("/exports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, UUID>> createExport(@AuthenticationPrincipal Jwt jwt,
                                                           @RequestParam String type) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(Map.of("id", service.createExport(userId(jwt), type)));
    }

    @GetMapping(value = "/exports/{id}/download", produces = "text/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadExport(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        AdminExportFile file = service.downloadExport(id, userId(jwt));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(file.content());
    }

    @PostMapping("/campaigns")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, UUID>> createCampaign(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody com.greenocean.backend.moderation.dto.CreateCampaignRequest request) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(Map.of("id", service.createCampaign(userId(jwt), request)));
    }

    @PostMapping("/policies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, UUID>> createPolicy(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody com.greenocean.backend.moderation.dto.CreatePolicyRequest request) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(Map.of("id", service.createPolicy(userId(jwt), request)));
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> userStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
            @Valid @RequestBody com.greenocean.backend.moderation.dto.AdminStatusRequest request) {
        service.userStatus(id, userId(jwt), request); return ResponseEntity.noContent().build();
    }

    @PatchMapping("/communities/{id}/status")
    public ResponseEntity<Void> communityStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
            @Valid @RequestBody com.greenocean.backend.moderation.dto.AdminStatusRequest request) {
        service.communityStatus(id, userId(jwt), request); return ResponseEntity.noContent().build();
    }

    @PatchMapping("/credential-documents/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> documentStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
            @Valid @RequestBody com.greenocean.backend.moderation.dto.AdminStatusRequest request) {
        service.documentStatus(id, userId(jwt), request); return ResponseEntity.noContent().build();
    }

    @PatchMapping("/support-requests/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supportRequest(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
            @Valid @RequestBody com.greenocean.backend.moderation.dto.AdminStatusRequest request) {
        service.supportRequest(id, userId(jwt), request); return ResponseEntity.noContent().build();
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
