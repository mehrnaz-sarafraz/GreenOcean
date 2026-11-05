package com.greenocean.backend.trust;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;import org.springframework.http.HttpStatus;import java.util.*;
@RestController @RequestMapping("/api/v1/operations")
public class OperationsController {
 private final IncidentOperations incidents;private final OperationsAlerts alerts;private final OperationsCommunications communications;private final ServiceQuality quality;private final RankingAssurance ranking;
 public OperationsController(IncidentOperations incidents,OperationsAlerts alerts,OperationsCommunications communications,ServiceQuality quality,RankingAssurance ranking){this.incidents=incidents;this.alerts=alerts;this.communications=communications;this.quality=quality;this.ranking=ranking;}
 public record Detection(@NotBlank String domain,@NotBlank String kind,@NotBlank String severity,@NotNull UUID retryKey,@NotBlank @Size(max=160)String evidenceReference){}
 public record Transition(@Min(1)long revision,@NotBlank String state,@NotBlank @Size(max=160)String evidenceReference){}
 public record Control(@Min(1)long revision,@NotBlank String control,@NotBlank String action,UUID targetUser,@NotBlank @Size(max=160)String evidenceReference){}
 public record Revision(@Min(1)long revision){}
 public record Classification(@Min(1)long revision,@NotBlank String kind,@NotBlank String severity,@NotBlank @Size(max=160)String evidenceReference){}
 public record Ownership(@Min(1)long revision,@NotBlank @Size(max=160)String evidenceReference){}
 public record Route(@NotBlank String domain,@NotNull UUID recipient,@NotBlank String destination,int level,@NotNull @Future java.time.Instant expiresAt){}
 public record Communication(@Min(1)long revision,@NotBlank String template,@NotBlank String locale,boolean publicStatus,@NotNull @Size(max=50)List<UUID> targets){}
 public record Audit(@NotNull UUID policyId,@NotBlank String profession,@NotBlank String mode,@Min(1)@Max(90)int intervalDays,@NotBlank @Size(max=160)String evidenceReference){}
 UUID who(Jwt j){return UUID.fromString(j.getSubject());}UUID sid(Jwt j){return UUID.fromString(j.getClaimAsString("sid"));}
 @GetMapping("/incidents") public Object queue(@AuthenticationPrincipal Jwt j,@RequestParam String domain){return incidents.queue(who(j),sid(j),domain);}
 @PostMapping("/incidents") @ResponseStatus(HttpStatus.CREATED)public Object detect(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Detection in){return Map.of("id",incidents.detect(who(j),sid(j),in.domain(),in.kind(),in.severity(),in.retryKey(),in.evidenceReference()));}
 @GetMapping("/incidents/{id}")public Object detail(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return incidents.detail(who(j),sid(j),id);}
 @PostMapping("/incidents/{id}/transition")@ResponseStatus(HttpStatus.NO_CONTENT)public void transition(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Transition in){incidents.transition(who(j),sid(j),id,in.revision(),in.state(),in.evidenceReference());}
 @PostMapping("/incidents/{id}/classification")@ResponseStatus(HttpStatus.NO_CONTENT)public void classification(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Classification in){incidents.classify(who(j),sid(j),id,in.revision(),in.kind(),in.severity(),in.evidenceReference());}
 @PostMapping("/incidents/{id}/claim-unavailable")@ResponseStatus(HttpStatus.NO_CONTENT)public void claim(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Ownership in){incidents.claimUnavailable(who(j),sid(j),id,in.revision(),in.evidenceReference());}
 @PostMapping("/incidents/{id}/controls")@ResponseStatus(HttpStatus.CREATED)public Object control(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Control in){return Map.of("id",incidents.propose(who(j),sid(j),id,in.revision(),in.control(),in.action(),in.targetUser(),in.evidenceReference()));}
 @PostMapping("/incidents/{id}/controls/{control}/approve")@ResponseStatus(HttpStatus.NO_CONTENT)public void approve(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@PathVariable UUID control,@Valid @RequestBody Revision in){incidents.approve(who(j),sid(j),id,control,in.revision());}
 @GetMapping("/quality") public Object quality(@AuthenticationPrincipal Jwt j,@RequestParam String domain){return quality.summary(who(j),sid(j),domain);}
 @GetMapping("/alert-routes")public Object routes(@AuthenticationPrincipal Jwt j,@RequestParam String domain){return alerts.routes(who(j),sid(j),domain);}
 @PostMapping("/alert-routes")@ResponseStatus(HttpStatus.CREATED)public Object route(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Route in){return Map.of("id",alerts.route(who(j),sid(j),in.domain(),in.recipient(),in.destination(),in.level(),in.expiresAt()));}
 @PostMapping("/alert-routes/{id}/approve")@ResponseStatus(HttpStatus.NO_CONTENT)public void approveRoute(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){alerts.approve(who(j),sid(j),id);}
 @PostMapping("/alert-routes/{id}/acknowledge")@ResponseStatus(HttpStatus.NO_CONTENT)public void acknowledgeRoute(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){alerts.acknowledgeRoute(who(j),sid(j),id);}
 @DeleteMapping("/alert-routes/{id}")@ResponseStatus(HttpStatus.NO_CONTENT)public void revokeRoute(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){alerts.revoke(who(j),sid(j),id);}
 @GetMapping("/alerts")public Object deliveries(@AuthenticationPrincipal Jwt j,@RequestParam String domain){return alerts.deliveries(who(j),sid(j),domain);}
 @PostMapping("/alerts/{id}/acknowledge")@ResponseStatus(HttpStatus.NO_CONTENT)public void acknowledge(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){alerts.acknowledge(who(j),sid(j),id);}
 @GetMapping("/incidents/{id}/communications")public Object communications(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return communications.queue(who(j),sid(j),id);}
 @PostMapping("/incidents/{id}/communications")@ResponseStatus(HttpStatus.CREATED)public Object communication(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Communication in){return Map.of("id",communications.draft(who(j),sid(j),id,in.revision(),in.template(),in.locale(),in.publicStatus(),in.targets()));}
 @PostMapping("/incidents/{id}/communications/{communication}/approve")@ResponseStatus(HttpStatus.NO_CONTENT)public void approveCommunication(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@PathVariable UUID communication,@Valid @RequestBody Revision in){communications.approve(who(j),sid(j),id,communication,in.revision());}
 @GetMapping("/ranking-audits")public Object audits(@AuthenticationPrincipal Jwt j){return ranking.list(who(j),sid(j));}
 @PostMapping("/ranking-audits")@ResponseStatus(HttpStatus.CREATED)public Object audit(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Audit in){return Map.of("id",ranking.propose(who(j),sid(j),in.policyId(),in.profession(),in.mode(),in.intervalDays(),in.evidenceReference()));}
 @PostMapping("/ranking-audits/{id}/approve")@ResponseStatus(HttpStatus.NO_CONTENT)public void approveAudit(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){ranking.approve(who(j),sid(j),id);}
 @DeleteMapping("/ranking-audits/{id}")@ResponseStatus(HttpStatus.NO_CONTENT)public void revokeAudit(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){ranking.revoke(who(j),sid(j),id);}
}
