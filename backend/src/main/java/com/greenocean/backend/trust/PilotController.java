package com.greenocean.backend.trust;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/pilot")
public class PilotController {
    private final PilotReadiness readiness;private final PilotParticipants participants;
    public PilotController(PilotReadiness readiness,PilotParticipants participants){this.readiness=readiness;this.participants=participants;}
    UUID who(Jwt j){return UUID.fromString(j.getSubject());}UUID sid(Jwt j){return UUID.fromString(j.getClaimAsString("sid"));}
    public record Create(@NotBlank String service,@Pattern(regexp="[A-Z]{2}")String countryCode,@Pattern(regexp="[a-f0-9]{40}")String sourceCommit){}
    public record Revision(@Min(1)long revision){}
    public record Invite(@NotNull UUID userId){}
    public record Choice(@Min(1)long revision,boolean accept){}
    @GetMapping("/assessments")public Object list(@AuthenticationPrincipal Jwt j){return readiness.list(who(j),sid(j));}
    @PostMapping("/assessments") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)public Object create(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Create p){return Map.of("id",readiness.create(who(j),sid(j),p.service(),p.countryCode(),p.sourceCommit()));}
    @GetMapping("/assessments/{id}")public Object detail(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return readiness.detail(who(j),sid(j),id);}
    @PostMapping("/assessments/{id}/evidence")public Object evidence(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestBody PilotReadiness.Evidence p){return Map.of("id",readiness.evidence(who(j),sid(j),id,p));}
    @PostMapping("/assessments/{id}/evidence/{e}/review") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)public void review(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@PathVariable UUID e,@Valid @RequestBody Revision p){readiness.review(who(j),sid(j),id,e,p.revision());}
    @PostMapping("/assessments/{id}/owner-approval") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)public void approve(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Revision p){readiness.approve(who(j),sid(j),id,p.revision());}
    @PostMapping("/assessments/{id}/revoke") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)public void revoke(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){readiness.revoke(who(j),sid(j),id);}
    @PostMapping("/assessments/{id}/invitations")public Object invite(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Invite p){return Map.of("id",participants.invite(who(j),sid(j),id,p.userId()));}
    @GetMapping("/me")public Object me(@AuthenticationPrincipal Jwt j){return participants.mine(who(j));}
    @PostMapping("/invitations/{id}/choice") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)public void choice(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Choice p){participants.choice(who(j),id,p.revision(),p.accept());}
    @PostMapping("/invitations/{id}/revoke") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)public void remove(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){participants.revoke(who(j),sid(j),id);}
}
