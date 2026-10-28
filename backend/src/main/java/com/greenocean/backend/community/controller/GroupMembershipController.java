package com.greenocean.backend.community.controller;
import com.greenocean.backend.community.service.*;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import org.springframework.web.bind.annotation.*;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import java.util.*;
@RestController @RequestMapping("/api/v1")
public class GroupMembershipController {
 private final GroupMemberships m;private final CommunityService communities;
 public GroupMembershipController(GroupMemberships m,CommunityService communities){this.m=m;this.communities=communities;}
 public record Invite(@NotNull UUID targetId,@NotNull UUID retryKey){}
 public record Action(@NotBlank @Pattern(regexp="ACCEPT|DECLINE|REVOKE")String action,@Min(0)long rulesVersion){}
 public record MemberAction(@NotBlank @Pattern(regexp="REMOVE|BAN|RESTRICT")String action,@NotBlank @Size(max=1000)String reason,@Future java.time.Instant until){}
 public record Target(@NotNull UUID targetId){}
 public record Decision(boolean accepted){}
 @GetMapping("/communities/mine") public List<Map<String,Object>> mine(@AuthenticationPrincipal Jwt jwt,@RequestParam(defaultValue="0")int page){return m.mine(user(jwt),page);}
 @PutMapping("/communities/{group}/rules-ack") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void acknowledge(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group,@Valid @RequestBody CommunityController.RulesAck in){m.acknowledge(group,user(jwt),in.rulesVersion());}
 @PostMapping("/communities/{group}/members/{target}/action") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void memberAction(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group,@PathVariable UUID target,@Valid @RequestBody MemberAction in){m.remove(group,target,user(jwt),in.action(),in.reason(),in.until());}
 @DeleteMapping("/communities/{group}/bans/{target}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void unban(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group,@PathVariable UUID target){m.unban(group,target,user(jwt));}
 @GetMapping("/communities/{group}/bans") public List<Map<String,Object>> bans(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group){return m.bans(group,user(jwt));}
 @GetMapping("/community-invitations/mine") public List<Map<String,Object>> inbox(@AuthenticationPrincipal Jwt jwt){return m.inbox(user(jwt));}
 @GetMapping("/communities/{group}/invitations") public List<Map<String,Object>> sent(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group){return m.invitations(group,user(jwt));}
 @PostMapping("/communities/{group}/invitations") @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public Map<String,Object> invite(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group,@Valid @RequestBody Invite in){return m.invite(group,user(jwt),in.targetId(),in.retryKey());}
 @PostMapping("/community-invitations/{id}/decision") public Map<String,Object> invitation(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Action in){UUID group=m.invitation(id,user(jwt),in.action(),in.rulesVersion());if("ACCEPT".equals(in.action()))return Map.of("community",communities.getById(group,user(jwt)));return Map.of("status","UPDATED");}
 @PostMapping("/communities/{group}/ownership-transfer") @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public Map<String,UUID> transfer(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group,@Valid @RequestBody Target in){return Map.of("id",m.transfer(group,user(jwt),in.targetId()));}
 @DeleteMapping("/communities/{group}/ownership-transfer") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void cancelTransfer(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group){m.cancelTransfer(group,user(jwt));}
 @GetMapping("/community-transfers/mine") public List<Map<String,Object>> transfers(@AuthenticationPrincipal Jwt jwt){return m.transfers(user(jwt));}
 @PostMapping("/community-transfers/{id}/decision") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void accept(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Decision in){m.acceptTransfer(id,user(jwt),in.accepted());}
 private UUID user(Jwt jwt){return UUID.fromString(jwt.getSubject());}
}
