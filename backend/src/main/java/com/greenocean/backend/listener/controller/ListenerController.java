package com.greenocean.backend.listener.controller;

import com.greenocean.backend.listener.dto.*;
import com.greenocean.backend.listener.service.ListenerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ListenerController {
    private final ListenerService service;
    public ListenerController(ListenerService service) { this.service = service; }

    @GetMapping("/listeners")
    public List<ListenerResponse> listeners(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "false") boolean onlineOnly,
            @RequestParam(required = false) String topic) {
        return service.listeners(userId(jwt), onlineOnly, topic);
    }

    @PostMapping("/listeners/match")
    public ListenerMatchResponse match(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) UUID listenerId, @RequestParam(required = false) String topic) {
        return service.match(userId(jwt), listenerId, topic);
    }

    @PostMapping("/listener-applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ListenerApplicationResponse apply(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ListenerApplicationRequest request) { return service.apply(userId(jwt), request); }

    @GetMapping("/listener-applications/me")
    public ListenerApplicationResponse application(@AuthenticationPrincipal Jwt jwt) {
        return service.application(userId(jwt));
    }

    @GetMapping("/listener-training/me")
    public ListenerTrainingResponse training(@AuthenticationPrincipal Jwt jwt) {
        return service.training(userId(jwt));
    }

    @PutMapping("/listener-training/me/modules/{moduleKey}")
    public ListenerTrainingResponse completeTrainingModule(@AuthenticationPrincipal Jwt jwt,
                                                            @PathVariable String moduleKey) {
        return service.completeTrainingModule(userId(jwt), moduleKey);
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
