package com.greenocean.backend.community.controller;

import com.greenocean.backend.community.service.*;
import com.greenocean.backend.community.dto.CreateCommunityEventRequest;
import com.greenocean.backend.moderation.dto.CreateReportRequest;
import com.greenocean.backend.post.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1")
public class GroupSafetyController {
 private final GroupActivities activities;private final GroupSafety safety;
 public GroupSafetyController(GroupActivities activities,GroupSafety safety){this.activities=activities;this.safety=safety;}
 public record Lifecycle(@NotBlank String status,@NotBlank @Size(max=1000) String reason){}
 public record Reason(@NotBlank @Size(max=1000)String reason){}
 public record ActivityUpdate(@Min(1)long revision,@NotNull @Valid CreateCommunityEventRequest activity){}
 public record Transition(@Min(1)long revision,@NotBlank String status){}
 public record Discussion(@Min(1)long revision,@NotNull @Valid CreatePostRequest post){}
 public record Hide(@Min(1)long revision,@NotBlank @Size(max=1000)String reason){}
 public record AppealDecision(boolean restore,@NotBlank @Size(max=1000)String note){}
 public record ReportDecision(@NotBlank String outcome,@NotBlank @Size(max=1000)String note){}
 @PutMapping("/communities/{group}/events/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void update(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group,@PathVariable UUID id,@Valid @RequestBody ActivityUpdate in){activities.save(group,user(jwt),id,in.revision(),in.activity());}
 @PostMapping("/communities/{group}/events/{id}/status") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void status(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group,@PathVariable UUID id,@Valid @RequestBody Transition in){activities.transition(group,id,user(jwt),in.revision(),in.status());}
 @PostMapping("/communities/{group}/events/{id}/discussion") public PostResponse discussion(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group,@PathVariable UUID id,@Valid @RequestBody Discussion in){return activities.discussion(group,id,user(jwt),in.revision(),in.post());}
 @PostMapping("/communities/{group}/reports") @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public Map<String,UUID> report(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group,@Valid @RequestBody CreateReportRequest in){return Map.of("id",safety.report(group,user(jwt),in));}
 @GetMapping("/communities/{group}/reports") public List<Map<String,Object>> queue(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group){return safety.queue(group,user(jwt));}
 @PostMapping("/communities/{group}/reports/{id}/hide") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void hide(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group,@PathVariable UUID id,@Valid @RequestBody Hide in){safety.hide(group,id,user(jwt),in.revision(),in.reason());}
 @GetMapping("/community-actions/mine") public List<Map<String,Object>> actions(@AuthenticationPrincipal Jwt jwt){return safety.actions(user(jwt));}
 @PostMapping("/community-actions/{id}/appeal") @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public Map<String,UUID> appeal(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Reason in){return Map.of("id",safety.appeal(user(jwt),id,in.reason()));}
 @PostMapping("/communities/{group}/close") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void close(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group,@Valid @RequestBody Reason in){safety.lifecycle(group,user(jwt),"ARCHIVED",in.reason(),false);}
 @PostMapping("/admin/groups/{group}/status") @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void lifecycle(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID group,@Valid @RequestBody Lifecycle in){safety.lifecycle(group,user(jwt),in.status(),in.reason(),true);}
 @GetMapping("/admin/groups/reports") @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')") public List<Map<String,Object>> staffQueue(@AuthenticationPrincipal Jwt jwt,@RequestParam String purpose){return safety.staffQueue(user(jwt),purpose);}
 @GetMapping("/admin/groups/appeals") @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')") public List<Map<String,Object>> staffAppeals(@AuthenticationPrincipal Jwt jwt,@RequestParam String purpose){return safety.appeals(user(jwt),purpose);}
 @PostMapping("/admin/groups/reports/{id}/decision") @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void reportDecision(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody ReportDecision in){safety.resolveReport(user(jwt),id,in.outcome(),in.note());}
 @PostMapping("/admin/groups/appeals/{id}/decision") @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void decide(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody AppealDecision in){safety.decideAppeal(user(jwt),id,in.restore(),in.note());}
 private UUID user(Jwt jwt){return UUID.fromString(jwt.getSubject());}
}
