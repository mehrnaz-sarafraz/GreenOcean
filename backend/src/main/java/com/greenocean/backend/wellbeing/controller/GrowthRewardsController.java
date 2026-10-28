package com.greenocean.backend.wellbeing.controller;
import com.greenocean.backend.wellbeing.service.GrowthRewards;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.*;
@RestController
public class GrowthRewardsController {
 private final GrowthRewards service;public GrowthRewardsController(GrowthRewards service){this.service=service;}
 public record Reason(String reason){}
 @GetMapping("/api/v1/wellbeing/growth-rewards") public Map<String,Object> own(@AuthenticationPrincipal Jwt jwt,@RequestParam(defaultValue="0")int page){return service.dashboard(UUID.fromString(jwt.getSubject()),page);}
 @PostMapping("/api/v1/admin/growth-rewards/{event}/reverse") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void reverse(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID event,@RequestBody Reason input){service.reverse(UUID.fromString(jwt.getSubject()),event,input.reason());}
 @PostMapping("/api/v1/admin/growth-rewards/owners/{owner}/reconcile") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void reconcile(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID owner,@RequestBody Reason input){service.reconcile(UUID.fromString(jwt.getSubject()),owner,input.reason());}
}
