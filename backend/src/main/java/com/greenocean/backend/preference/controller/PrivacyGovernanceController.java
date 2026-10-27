package com.greenocean.backend.preference.controller;
import com.greenocean.backend.preference.service.PrivacyGovernanceService;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/account/privacy")
public class PrivacyGovernanceController {
 private final PrivacyGovernanceService service;
 public PrivacyGovernanceController(PrivacyGovernanceService service){this.service=service;}
 public record Decision(@NotBlank @Size(max=40) String purpose,@NotBlank @Pattern(regexp="GRANTED|DENIED|WITHDRAWN") String decision,UUID noticeId){}
 public record Receipt(@NotNull UUID noticeId){}
 public record Rights(@NotBlank @Size(max=24) String kind,@NotNull UUID idempotencyKey){}
 @GetMapping public Map<String,Object> get(@AuthenticationPrincipal Jwt jwt){return service.snapshot(owner(jwt));}
 @PostMapping("/decisions") public Map<String,Object> decide(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Decision input){return service.decide(owner(jwt),input.purpose(),input.decision(),input.noticeId());}
 @PostMapping("/receipts") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void receipt(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Receipt input){service.receiveNotice(owner(jwt),input.noticeId());}
 @PostMapping("/rights") public Map<String,Object> right(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Rights input){return service.requestRight(owner(jwt),input.kind(),input.idempotencyKey());}
 private UUID owner(Jwt jwt){return UUID.fromString(jwt.getSubject());}
}
