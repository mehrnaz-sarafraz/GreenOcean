package com.greenocean.backend.moderation.controller;
import com.greenocean.backend.moderation.service.SocialReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/v1")
public class SocialReviewController {
    private final SocialReviewService service;
    public SocialReviewController(SocialReviewService service){this.service=service;}
    public record Appeal(@NotBlank @Size(max=2000) String reason) {}
    public record Decision(@Min(1) long revision,@NotBlank String outcome,@NotBlank @Size(max=1000) String note) {}
    public record Purpose(@NotBlank @Size(min=10,max=500) String purpose) {}
    @GetMapping("/social-moderation/me") public List<Map<String,Object>> mine(@AuthenticationPrincipal Jwt jwt,@RequestParam(defaultValue="0") int page){return service.mine(user(jwt),page);}
    @PostMapping("/social-moderation/{id}/appeal") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public Map<String,UUID> appeal(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Appeal input){return Map.of("id",service.appeal(user(jwt),id,input.reason()));}
    @GetMapping("/admin/social-moderation") @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public List<Map<String,Object>> queue(@AuthenticationPrincipal Jwt jwt,@RequestParam(defaultValue="0") int page){return service.queue(user(jwt),page);}
    @PostMapping("/admin/social-moderation/{id}/decision") @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void decide(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Decision input){service.decide(user(jwt),id,input.revision(),input.outcome(),input.note());}
    @PostMapping("/admin/social-moderation/{id}/attribution") @PreAuthorize("hasRole('ADMIN')")
    public Map<String,Object> attribution(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Purpose input){return service.attribution(user(jwt),id,input.purpose());}
    private UUID user(Jwt jwt){return UUID.fromString(jwt.getSubject());}
}
