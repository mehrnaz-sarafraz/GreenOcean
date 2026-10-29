package com.greenocean.backend.professional.controller;
import com.greenocean.backend.professional.service.*;import org.springframework.web.bind.annotation.*;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import org.springframework.http.HttpStatus;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import java.util.*;import java.time.*;
@RestController @RequestMapping("/api/v1/clinic-integration")
public class ClinicIntegrationController {
 private final ClinicIntegration service;public ClinicIntegrationController(ClinicIntegration service){this.service=service;}private UUID who(Jwt j){return UUID.fromString(j.getSubject());}private UUID sid(Jwt j){return UUID.fromString(j.getClaimAsString("sid"));}
 public record Recommendation(@NotNull UUID appointmentId,UUID followupId,@Min(0)long revision,@NotBlank @Size(min=3,max=5000)String body,@NotNull Instant suggestedAfter,@NotNull Instant expiresAt){}
 public record Choice(@Min(1)long revision,@NotNull String choice,Instant remindAt){}
 public record Replacement(@Min(1)long revision,@NotNull @Valid ClinicCommerce.Booking booking){}
 public record Incident(@NotNull UUID appointmentId,@NotNull String kind){}
 public record Review(@NotBlank String classification,@NotBlank @Size(min=10,max=1000)String evidence){}
 public record Rights(UUID appointmentId,@NotBlank String category,@NotBlank String kind,@NotNull UUID retryKey){}
 public record Decision(@NotBlank String state,@NotBlank @Size(min=10,max=1000)String evidence){}
 public record Hold(@NotNull UUID subjectId,@NotBlank String category,@NotBlank @Size(min=10,max=1000)String evidence,@NotNull Instant until){}
 public record Preferences(boolean enabled,@NotBlank String zone,@Min(0) @Max(23)int quietStart,@Min(0) @Max(23)int quietEnd){}
 @GetMapping("/followups")public Object followups(@AuthenticationPrincipal Jwt j){return service.followups(who(j),sid(j));}
 @PostMapping("/followups")public Object recommend(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Recommendation p){return Map.of("id",service.recommend(who(j),sid(j),new ClinicIntegration.Recommendation(p.appointmentId(),p.followupId(),p.revision(),p.body(),p.suggestedAfter(),p.expiresAt())));}
 @PostMapping("/followups/{id}/choice") @ResponseStatus(HttpStatus.NO_CONTENT)public void choose(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Choice p){service.choose(who(j),sid(j),id,p.revision(),p.choice(),p.remindAt());}
 @PostMapping("/appointments/{id}/replacement")public Object replace(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Replacement p){return service.reschedule(who(j),sid(j),id,p.revision(),p.booking());}
 @PostMapping("/incidents")public Object incident(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Incident p){return Map.of("id",service.incident(who(j),sid(j),p.appointmentId(),p.kind()));}
 @PostMapping("/incidents/{id}/reviews")public Object review(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Review p){return Map.of("id",service.incidentReview(who(j),sid(j),id,p.classification(),p.evidence()));}
 @PostMapping("/incident-reviews/{id}/approve") @ResponseStatus(HttpStatus.NO_CONTENT)public void approve(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){service.approveIncident(who(j),sid(j),id);}
 @GetMapping("/data-classes")public Object manifest(@AuthenticationPrincipal Jwt j){return service.manifest(who(j),sid(j));}
 @PostMapping("/rights")public Object rights(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Rights p){return Map.of("id",service.rights(who(j),sid(j),p.appointmentId(),p.category(),p.kind(),p.retryKey()));}
 @GetMapping("/rights")public Object rights(@AuthenticationPrincipal Jwt j){return service.ownRights(who(j),sid(j));}
 @PostMapping("/rights/{id}/review") @ResponseStatus(HttpStatus.NO_CONTENT)public void rightsReview(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Decision p){service.reviewRights(who(j),sid(j),id,p.state(),p.evidence());}
 @PostMapping("/holds")public Object hold(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Hold p){return Map.of("id",service.hold(who(j),sid(j),p.subjectId(),p.category(),p.evidence(),p.until()));}
 @PostMapping("/holds/{id}/approve") @ResponseStatus(HttpStatus.NO_CONTENT)public void approveHold(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){service.approveHold(who(j),sid(j),id);}
 @GetMapping("/operations")public Object operations(@AuthenticationPrincipal Jwt j,@RequestParam String view){return service.operations(who(j),sid(j),view);}
 @GetMapping("/notification-preferences")public Object preferences(@AuthenticationPrincipal Jwt j){return service.preferences(who(j),sid(j));}
 @PutMapping("/notification-preferences") @ResponseStatus(HttpStatus.NO_CONTENT)public void preferences(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Preferences p){service.preferences(who(j),sid(j),p.enabled(),p.zone(),p.quietStart(),p.quietEnd());}
 @PostMapping("/dispatch-reminders")public Object reminders(@AuthenticationPrincipal Jwt j){return Map.of("created",service.reminders(who(j),sid(j)));}
}
