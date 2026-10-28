package com.greenocean.backend.catalog.controller;
import com.greenocean.backend.catalog.service.GrowthLibrary;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.*;
@RestController @RequestMapping("/api/v1/admin/education") @PreAuthorize("hasRole('ADMIN')")
public class EducationPermissionsController {
 private final GrowthLibrary s;public EducationPermissionsController(GrowthLibrary s){this.s=s;}
 private UUID user(Jwt j){return UUID.fromString(j.getSubject());}
 @GetMapping("/permissions") public List<Map<String,Object>> list(@AuthenticationPrincipal Jwt j,@RequestParam(defaultValue="0")int page){return s.grants(user(j),page);}
 @PostMapping("/permissions") public Map<String,UUID> grant(@AuthenticationPrincipal Jwt j,@RequestBody GrowthLibrary.Grant in){return Map.of("id",s.grant(user(j),in));}
 @PostMapping("/credentials") public Map<String,UUID> credential(@AuthenticationPrincipal Jwt j,@RequestBody GrowthLibrary.Credential in){return Map.of("id",s.credential(user(j),in));}
 @DeleteMapping("/permissions/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void revoke(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){s.revoke(user(j),id,false);}
 @DeleteMapping("/credentials/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void revokeCredential(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){s.revoke(user(j),id,true);}
}
