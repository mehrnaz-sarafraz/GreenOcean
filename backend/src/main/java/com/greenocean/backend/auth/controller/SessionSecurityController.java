package com.greenocean.backend.auth.controller;
import com.greenocean.backend.auth.service.*;import org.springframework.web.bind.annotation.*;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import java.util.*;
@RestController @RequestMapping("/api/v1/auth") public class SessionSecurityController {
 private final RecentAuthentication recent;private final SecurityEvents events;
 public SessionSecurityController(RecentAuthentication recent,SecurityEvents events){this.recent=recent;this.events=events;}
 public record Proof(@NotBlank @Size(max=256) String password){}
 @PostMapping("/reauthenticate") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void reauthenticate(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Proof proof){recent.password(UUID.fromString(jwt.getSubject()),UUID.fromString(jwt.getClaimAsString("sid")),proof.password());}
 @PostMapping("/activity") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void activity(@AuthenticationPrincipal Jwt jwt){recent.activity(UUID.fromString(jwt.getSubject()),UUID.fromString(jwt.getClaimAsString("sid")));}
 @GetMapping("/security-events") public List<Map<String,Object>> events(@AuthenticationPrincipal Jwt jwt){return events.mine(UUID.fromString(jwt.getSubject()));}
}
