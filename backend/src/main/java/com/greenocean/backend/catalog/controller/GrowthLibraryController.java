package com.greenocean.backend.catalog.controller;
import com.greenocean.backend.catalog.service.GrowthLibrary;
import com.greenocean.backend.auth.service.RecentAuthentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.*;
@RestController @RequestMapping("/api/v1/growth-library")
public class GrowthLibraryController {
 private final GrowthLibrary s;private final RecentAuthentication recent;public GrowthLibraryController(GrowthLibrary s,RecentAuthentication recent){this.s=s;this.recent=recent;}
 private UUID user(Jwt j){return UUID.fromString(j.getSubject());}private void strong(Jwt j){recent.require(user(j),UUID.fromString(j.getClaimAsString("sid")),true);}
 @GetMapping("/resources") public Map<String,Object> browse(@AuthenticationPrincipal Jwt j,@RequestParam(defaultValue="")String search,@RequestParam(defaultValue="")String kind,@RequestParam(defaultValue="")String topic,@RequestParam(defaultValue="")String language,@RequestParam(defaultValue="false")boolean saved,@RequestParam(defaultValue="0")int page){return s.browse(user(j),search,kind,topic,language,saved,page);}
 @GetMapping("/resources/{id}") public Map<String,Object> detail(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return s.detail(user(j),id);}
 @PutMapping("/resources/{id}/save") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void save(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){s.save(user(j),id,true);}
 @DeleteMapping("/resources/{id}/save") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void unsave(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){s.save(user(j),id,false);}
 @GetMapping("/editorial") public List<Map<String,Object>> workspace(@AuthenticationPrincipal Jwt j,@RequestParam(defaultValue="0")int page){return s.workspace(user(j),page);}
 @GetMapping("/editorial/{id}") public Map<String,Object> editorial(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return s.editorial(user(j),id);}
 @PostMapping("/editorial") public Map<String,Object> create(@AuthenticationPrincipal Jwt j,@RequestBody GrowthLibrary.Draft in){return s.create(user(j),in);}
 @PutMapping("/editorial/{id}") public Map<String,Object> edit(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestBody GrowthLibrary.Draft in){return s.edit(user(j),id,in,false);}
 @PostMapping("/editorial/{id}/revisions") public Map<String,Object> revision(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestBody GrowthLibrary.Draft in){return s.edit(user(j),id,in,true);}
 @PostMapping("/editorial/{id}/transitions") public Map<String,Object> transition(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestBody GrowthLibrary.Decision in){if(!"SUBMIT".equals(in.decision()))strong(j);return s.transition(user(j),id,in);}
 @GetMapping("/posts/{post}/preference") public Map<String,Object> preference(@AuthenticationPrincipal Jwt j,@PathVariable UUID post){return s.preference(user(j),post);}
 @PutMapping("/posts/{post}/preference") public Map<String,Object> preference(@AuthenticationPrincipal Jwt j,@PathVariable UUID post,@RequestBody GrowthLibrary.Preference in){return s.preference(user(j),post,in);}
 @GetMapping("/posts/{post}/resources") public List<Map<String,Object>> attachments(@AuthenticationPrincipal Jwt j,@PathVariable UUID post){return s.attachments(user(j),post);}
 @PostMapping("/posts/{post}/resources") public Map<String,Object> propose(@AuthenticationPrincipal Jwt j,@PathVariable UUID post,@RequestBody GrowthLibrary.Proposal in){return s.propose(user(j),post,in);}
 @GetMapping("/posts/{post}/proposals") public List<Map<String,Object>> proposals(@AuthenticationPrincipal Jwt j,@PathVariable UUID post){strong(j);return s.proposals(user(j),post);}
 @PostMapping("/posts/{post}/resources/{id}/approve") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void approve(@AuthenticationPrincipal Jwt j,@PathVariable UUID post,@PathVariable UUID id,@RequestBody Map<String,String> in){strong(j);s.reviewAttachment(user(j),post,id,in.get("note"));}
 @DeleteMapping("/posts/{post}/resources/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void remove(@AuthenticationPrincipal Jwt j,@PathVariable UUID post,@PathVariable UUID id){s.removeAttachment(user(j),post,id);}
}
