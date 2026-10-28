package com.greenocean.backend.listener.planning;
import jakarta.validation.Valid;import org.springframework.web.bind.annotation.*;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import java.util.*;
@RestController @RequestMapping("/api/v1/admin/listener-support/coverage") @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
public class ListenerQueueCoverageController {
 private final ListenerQueueCoverage coverage;public ListenerQueueCoverageController(ListenerQueueCoverage c){coverage=c;}
 private UUID who(Jwt j){return UUID.fromString(j.getSubject());}
 @GetMapping public List<Map<String,Object>> list(@AuthenticationPrincipal Jwt j,@RequestParam(defaultValue="0")int page){return coverage.mine(who(j),page);}
 @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public Map<String,UUID> approve(@AuthenticationPrincipal Jwt j,@Valid @RequestBody ListenerQueueCoverage.Window input){return Map.of("id",coverage.approve(who(j),input));}
 @PostMapping("/{id}/revoke") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void revoke(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){coverage.revoke(who(j),id);}
}
