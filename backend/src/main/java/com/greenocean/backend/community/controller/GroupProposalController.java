package com.greenocean.backend.community.controller;
import com.greenocean.backend.community.service.GroupProposals;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import java.util.*;
@RestController @RequestMapping("/api/v1")
public class GroupProposalController {
 private final GroupProposals service;public GroupProposalController(GroupProposals service){this.service=service;}
 public record Revision(@Min(1)long revision){}
 public record Review(@Min(1)long revision,@NotBlank String outcome,@NotBlank @Size(max=1000)String note){}
 @PostMapping("/community-proposals") @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public Map<String,Object> create(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody GroupProposals.Input in){return service.save(user(jwt),null,in);}
 @PutMapping("/community-proposals/{id}") public Map<String,Object> update(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody GroupProposals.Input in){return service.save(user(jwt),id,in);}
 @GetMapping("/community-proposals/mine") public List<Map<String,Object>> mine(@AuthenticationPrincipal Jwt jwt,@RequestParam(defaultValue="0")int page){return service.mine(user(jwt),page);}
 @GetMapping("/community-proposals/{id}") public Map<String,Object> own(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return service.own(user(jwt),id);}
 @PostMapping("/community-proposals/{id}/{action:submit|cancel}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void move(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@PathVariable String action,@Valid @RequestBody Revision in){service.transition(user(jwt),id,in.revision(),action.toUpperCase());}
 @GetMapping("/admin/community-proposals") @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')") public List<Map<String,Object>> queue(@AuthenticationPrincipal Jwt jwt,@RequestParam(defaultValue="0")int page){return service.queue(user(jwt),page);}
 @PostMapping("/admin/community-proposals/{id}/decision") @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')") public Map<String,Object> decide(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Review in){return service.decide(user(jwt),id,in.revision(),in.outcome(),in.note());}
 private UUID user(Jwt jwt){return UUID.fromString(jwt.getSubject());}
}
