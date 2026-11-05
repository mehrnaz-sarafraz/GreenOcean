package com.greenocean.backend.post.controller;
import com.greenocean.backend.post.service.SocialDrafts;
import com.greenocean.backend.post.dto.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/v1/posts/drafts")
public class DraftController {
 private final SocialDrafts drafts;
 public DraftController(SocialDrafts drafts){this.drafts=drafts;}
 @GetMapping public List<Map<String,Object>> list(@AuthenticationPrincipal Jwt jwt){return drafts.list(id(jwt));}
 @GetMapping("/{draft}") public Map<String,Object> get(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID draft){return drafts.get(id(jwt),draft);}
 @PostMapping public Map<String,Object> create(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody SocialDrafts.Save request){return drafts.save(id(jwt),null,request);}
 @PutMapping("/{draft}") public Map<String,Object> save(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID draft,@Valid @RequestBody SocialDrafts.Save request){return drafts.save(id(jwt),draft,request);}
 @DeleteMapping("/{draft}") public void discard(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID draft){drafts.discard(id(jwt),draft);}
 @PostMapping("/{draft}/publish") public PostResponse publish(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID draft,@Valid @RequestBody CreatePostRequest request){return drafts.publish(id(jwt),draft,request);}
 private UUID id(Jwt jwt){return UUID.fromString(jwt.getSubject());}
}
