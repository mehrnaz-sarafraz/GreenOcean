package com.greenocean.backend.preference.controller;
import com.greenocean.backend.preference.service.PolicyPublicationService;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;import org.springframework.web.bind.annotation.*;import java.util.*;
/** All /admin paths also pass the existing recent strong-authentication interceptor. */
@RestController @RequestMapping("/api/v1/admin/governance") @PreAuthorize("hasRole('ADMIN')")
public class GovernanceAdminController {
 private final PolicyPublicationService service;public GovernanceAdminController(PolicyPublicationService service){this.service=service;}
 public record Review(@NotBlank @Pattern(regexp="[A-Za-z0-9._/-]{3,100}") String reference){}
 public record Resolution(@NotBlank String status,@NotBlank @Pattern(regexp="[A-Za-z0-9._/-]{3,100}") String reference){}
 @PostMapping("/policies/{id}/review") public void review(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Review input){service.review(id,UUID.fromString(jwt.getSubject()),input.reference());}
 @PostMapping("/policies/{id}/publish") public void publish(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){service.publish(id,UUID.fromString(jwt.getSubject()));}
 @GetMapping("/rights") public List<Map<String,Object>> queue(){return service.rightsQueue();}
 @PostMapping("/rights/{id}") public void resolve(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Resolution input){service.resolveRight(id,UUID.fromString(jwt.getSubject()),input.status(),input.reference());}
}
