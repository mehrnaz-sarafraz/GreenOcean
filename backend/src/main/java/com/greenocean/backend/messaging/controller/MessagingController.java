package com.greenocean.backend.messaging.controller;

import com.greenocean.backend.messaging.dto.ConversationResponse;
import com.greenocean.backend.messaging.dto.MessageResponse;
import com.greenocean.backend.messaging.dto.SendMessageRequest;
import com.greenocean.backend.messaging.dto.SupportChannelResponse;
import com.greenocean.backend.messaging.service.MessagingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class MessagingController {
    private final MessagingService service;
    public MessagingController(MessagingService service) { this.service = service; }

    @GetMapping("/support-channels")
    public List<SupportChannelResponse> channels(@AuthenticationPrincipal Jwt jwt) { return service.channels(userId(jwt)); }

    @PutMapping("/support-channels/{id}/membership")
    public SupportChannelResponse join(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.join(id, userId(jwt));
    }

    @DeleteMapping("/support-channels/{id}/membership")
    public ResponseEntity<Void> leave(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.leave(id, userId(jwt));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conversations")
    public List<ConversationResponse> conversations(@AuthenticationPrincipal Jwt jwt) {
        return service.conversations(userId(jwt));
    }

    @PostMapping("/conversations/professionals/{professionalId}")
    public ResponseEntity<Map<String, UUID>> startProfessional(@AuthenticationPrincipal Jwt jwt,
                                                               @PathVariable UUID professionalId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", service.startProfessionalConversation(professionalId, userId(jwt))));
    }

    @GetMapping("/conversations/{id}/messages")
    public List<MessageResponse> messages(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.messages(id, userId(jwt));
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<MessageResponse> send(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                                 @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.send(id, userId(jwt), request.body()));
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
