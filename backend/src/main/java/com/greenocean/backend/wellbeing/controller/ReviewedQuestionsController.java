package com.greenocean.backend.wellbeing.controller;
import com.greenocean.backend.wellbeing.service.ReviewedQuestions;
import com.greenocean.backend.auth.service.RecentAuthentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.*;
@RestController @RequestMapping("/api/v1/growth-library/questions")
public class ReviewedQuestionsController {
 private final ReviewedQuestions s;private final RecentAuthentication recent;public ReviewedQuestionsController(ReviewedQuestions s,RecentAuthentication recent){this.s=s;this.recent=recent;}
 private UUID user(Jwt j){return UUID.fromString(j.getSubject());}
 @GetMapping public List<Map<String,Object>> workspace(@AuthenticationPrincipal Jwt j,@RequestParam(defaultValue="0")int page){return s.workspace(user(j),page);}
 @PostMapping public Map<String,Object> create(@AuthenticationPrincipal Jwt j,@RequestBody ReviewedQuestions.Input in){return s.save(user(j),null,in,false);}
 @PutMapping("/{id}") public Map<String,Object> edit(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestBody ReviewedQuestions.Input in){return s.save(user(j),id,in,false);}
 @PostMapping("/{id}/revisions") public Map<String,Object> fork(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestBody ReviewedQuestions.Input in){return s.save(user(j),id,in,true);}
 @PostMapping("/{id}/transitions") public Map<String,Object> act(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestBody ReviewedQuestions.Action in){if(!"SUBMIT".equals(in.action()))recent.require(user(j),UUID.fromString(j.getClaimAsString("sid")),true);return s.act(user(j),id,in);}
}
