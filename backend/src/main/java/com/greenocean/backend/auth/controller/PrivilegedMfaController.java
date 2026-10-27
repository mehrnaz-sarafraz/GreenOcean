package com.greenocean.backend.auth.controller;
import com.greenocean.backend.auth.service.*;import org.springframework.web.bind.annotation.*;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import java.util.*;
@RestController @RequestMapping("/api/v1/auth/mfa") public class PrivilegedMfaController {
 private final PrivilegedWebAuthn mfa;public PrivilegedMfaController(PrivilegedWebAuthn mfa){this.mfa=mfa;}
 public record Begin(@Size(max=256) String permit){} public record Finish(@NotNull UUID ceremonyId,@NotBlank @Size(max=32768) String credentialJson,@Size(max=80) String label,@Size(max=256) String refreshToken){}
 private UUID user(Jwt jwt){return UUID.fromString(jwt.getSubject());}private UUID session(Jwt jwt){return UUID.fromString(jwt.getClaimAsString("sid"));}
 @GetMapping public Map<String,Object> status(@AuthenticationPrincipal Jwt jwt){return Map.of("available",mfa.available(),"credentials",mfa.credentials(user(jwt)));}
 @PostMapping("/registration/options") public PrivilegedWebAuthn.Start register(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Begin input)throws Exception{return mfa.register(user(jwt),session(jwt),input.permit());}
 @PostMapping("/authentication/options") public PrivilegedWebAuthn.Start authenticate(@AuthenticationPrincipal Jwt jwt)throws Exception{return mfa.authenticate(user(jwt),session(jwt));}
 @PostMapping("/registration/verify") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void register(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Finish input){mfa.finishRegistration(user(jwt),session(jwt),input.ceremonyId(),input.credentialJson(),input.label());}
 @PostMapping("/authentication/verify") public com.greenocean.backend.auth.dto.TokenResponse authenticate(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Finish input){return mfa.completeAuthentication(user(jwt),session(jwt),input.ceremonyId(),input.credentialJson(),input.refreshToken());}
 @DeleteMapping("/{credentialId}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void remove(@AuthenticationPrincipal Jwt jwt,@PathVariable String credentialId){mfa.remove(user(jwt),session(jwt),credentialId);}
}
