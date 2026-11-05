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
    private final SocialReviewService service;private final com.greenocean.backend.moderation.service.SocialAttribution attribution;
    public SocialReviewController(SocialReviewService service,com.greenocean.backend.moderation.service.SocialAttribution attribution){this.service=service;this.attribution=attribution;}
    public record Appeal(@NotBlank @Size(max=2000) String reason) {}
    public record Decision(@Min(1) long revision,@NotBlank String outcome,@NotBlank @Size(max=1000) String note) {}
    public record Revision(@Min(1) long revision) {}
    public record Purpose(@NotNull UUID requestId) {}
    public record EvidenceRequest(@Min(1)long revision,@NotBlank String purpose) {}
    @GetMapping("/social-moderation/me") public List<Map<String,Object>> mine(@AuthenticationPrincipal Jwt jwt,@RequestParam(defaultValue="0") int page){return service.mine(user(jwt),page);}
    @PostMapping("/social-moderation/{id}/appeal") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public Map<String,UUID> appeal(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Appeal input){return Map.of("id",service.appeal(user(jwt),id,input.reason()));}
    @GetMapping("/admin/social-moderation") @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public List<Map<String,Object>> queue(@AuthenticationPrincipal Jwt jwt,@RequestParam(defaultValue="0") int page){return service.queue(user(jwt),page);}
    @PostMapping("/admin/social-moderation/{id}/begin-review") @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void begin(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Revision input){service.beginReview(user(jwt),id,input.revision());}
    @PostMapping("/admin/social-moderation/{id}/decision") @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void decide(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Decision input){service.decide(user(jwt),id,input.revision(),input.outcome(),input.note());}
    @PostMapping("/admin/social-moderation/{id}/attribution") @PreAuthorize("hasRole('ADMIN')")
    public Map<String,Object> attribution(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Purpose input){return attribution.reveal(user(jwt),session(jwt),id,input.requestId());}
    @PostMapping("/admin/social-moderation/{id}/attribution-requests") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public Object requestEvidence(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody EvidenceRequest in){return Map.of("id",attribution.request(user(jwt),session(jwt),id,in.revision(),in.purpose()));}
    @GetMapping("/admin/social-attribution-requests") @PreAuthorize("hasRole('ADMIN')") public Object evidenceQueue(@AuthenticationPrincipal Jwt jwt){return attribution.queue(user(jwt),session(jwt));}
    @PostMapping("/admin/social-attribution-requests/{id}/approve") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void approveEvidence(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Revision in){attribution.approve(user(jwt),session(jwt),id,in.revision());}
    @DeleteMapping("/admin/social-attribution-requests/{id}") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void revokeEvidence(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){attribution.revoke(user(jwt),session(jwt),id);}
    private UUID session(Jwt jwt){return UUID.fromString(jwt.getClaimAsString("sid"));}
    private UUID user(Jwt jwt){return UUID.fromString(jwt.getSubject());}
}
