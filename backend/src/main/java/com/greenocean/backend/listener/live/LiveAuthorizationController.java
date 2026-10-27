package com.greenocean.backend.listener.live;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/admin/listener-live-authorizations")
@PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
public class LiveAuthorizationController {
 private final LiveSupportService service;
 public LiveAuthorizationController(LiveSupportService service){this.service=service;}
 public record Review(boolean practiceApproved,@Min(1) int conductVersion){}
 @PutMapping("/{applicationId}") public void review(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID applicationId,@Valid @RequestBody Review input){
  service.authorizePractice(applicationId,UUID.fromString(jwt.getSubject()),input.practiceApproved(),input.conductVersion());
 }
}
