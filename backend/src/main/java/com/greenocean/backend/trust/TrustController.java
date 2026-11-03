package com.greenocean.backend.trust;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api/v1/trust")
public class TrustController {
    private final TrustCases cases;private final TrustCoverage coverage;
    public TrustController(TrustCases cases,TrustCoverage coverage){this.cases=cases;this.coverage=coverage;}
    private UUID who(Jwt j){return UUID.fromString(j.getSubject());}private UUID sid(Jwt j){return UUID.fromString(j.getClaimAsString("sid"));}
    public record Import(@NotBlank String sourceKind,@NotNull UUID sourceId,@NotBlank String domain){}
    public record Move(@NotBlank String domain,@Min(1)long revision,@NotBlank String state){}
    public record Classification(@NotBlank String domain,@Min(1)long revision,@NotBlank String category,@NotBlank String severity){}
    public record Shift(@NotBlank String domain,@NotNull UUID operatorId,@NotNull Instant startsAt,@NotNull Instant endsAt,@Min(1)@Max(50)int capacity){}
    @GetMapping("/cases")public Object queue(@AuthenticationPrincipal Jwt j,@RequestParam String domain,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="false")boolean assigned){return cases.queue(who(j),sid(j),domain,page,assigned);}
    @GetMapping("/candidates")public Object candidates(@AuthenticationPrincipal Jwt j,@RequestParam String domain){return cases.candidates(who(j),sid(j),domain);}
    @GetMapping("/cases/{id}")public Object detail(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestParam String domain){return cases.detail(who(j),sid(j),id,domain);}
    @PostMapping("/cases") @ResponseStatus(HttpStatus.CREATED)public Object coordinate(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Import in){return Map.of("id",cases.coordinate(who(j),sid(j),in.sourceKind(),in.sourceId(),in.domain()));}
    @PostMapping("/cases/{id}/transition") @ResponseStatus(HttpStatus.NO_CONTENT)public void move(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Move in){cases.move(who(j),sid(j),id,in.domain(),in.revision(),in.state());}
    @PostMapping("/cases/{id}/classification") @ResponseStatus(HttpStatus.NO_CONTENT)public void classify(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@Valid @RequestBody Classification in){cases.classify(who(j),sid(j),id,in.domain(),in.revision(),in.category(),in.severity());}
    @GetMapping("/coverage")public Object shifts(@AuthenticationPrincipal Jwt j,@RequestParam String domain){return coverage.shifts(who(j),sid(j),domain);}
    @PostMapping("/coverage") @ResponseStatus(HttpStatus.CREATED)public Object schedule(@AuthenticationPrincipal Jwt j,@Valid @RequestBody Shift in){return Map.of("id",coverage.schedule(who(j),sid(j),in.domain(),in.operatorId(),in.startsAt(),in.endsAt(),in.capacity()));}
    @PostMapping("/coverage/{id}/acknowledge") @ResponseStatus(HttpStatus.NO_CONTENT)public void acknowledge(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){coverage.acknowledge(who(j),sid(j),id);}
    @DeleteMapping("/coverage/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)public void revoke(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){coverage.revoke(who(j),sid(j),id);}
}
