package com.greenocean.backend.trust;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import org.springframework.http.HttpStatus;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import java.util.*;
@RestController @RequestMapping("/api/v1/recovery")
public class RecoveryReplayController {
 private final RecoveryReplay replay;public RecoveryReplayController(RecoveryReplay replay){this.replay=replay;}private UUID who(Jwt j){return UUID.fromString(j.getSubject());}private UUID sid(Jwt j){return UUID.fromString(j.getClaimAsString("sid"));}
 public record Prepare(@NotBlank String sourceCommit,@NotBlank String manifestSha256,@NotNull @Size(max=5000)List<RecoveryReplay.Subject> subjects,@NotNull @Size(max=5)List<String> stoppedServices){}
 @GetMapping("/status")public Object status(@AuthenticationPrincipal Jwt j){return replay.status(who(j),sid(j));}
 @GetMapping("/runs/{id}")public Object detail(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return replay.detail(who(j),sid(j),id);}
 @PostMapping("/prepare") @ResponseStatus(HttpStatus.CREATED)public Object prepare(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Prepare in){return Map.of("id",replay.prepare(who(j),sid(j),in.sourceCommit(),in.manifestSha256(),in.subjects(),in.stoppedServices()));}
 @PostMapping("/runs/{id}/review") @ResponseStatus(HttpStatus.NO_CONTENT)public void review(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){replay.review(who(j),sid(j),id);}
 @PostMapping("/runs/{id}/invalidate") @ResponseStatus(HttpStatus.NO_CONTENT)public void invalidate(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){replay.invalidate(who(j),sid(j),id);}
}
