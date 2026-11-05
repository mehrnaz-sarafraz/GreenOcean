package com.greenocean.backend.wellbeing.controller;
import com.greenocean.backend.wellbeing.service.PrivateWellbeing;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.*;
@RestController
@RequestMapping("/api/v1/wellbeing")
public class PrivateWellbeingController {
 private final PrivateWellbeing s;public PrivateWellbeingController(PrivateWellbeing s){this.s=s;}
 private UUID user(Jwt jwt){return UUID.fromString(jwt.getSubject());}
 @GetMapping({"/journal","/check-ins","/goals"}) public Map<String,Object> history(@AuthenticationPrincipal Jwt j,jakarta.servlet.http.HttpServletRequest r,@RequestParam(defaultValue="20")int size,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="")String search,@RequestParam(defaultValue="false")boolean oldest){return s.history(user(j),r.getRequestURI().substring(r.getRequestURI().lastIndexOf('/')+1),size,page,search,oldest);}
 @GetMapping("/journal/{id}") public Map<String,Object> journal(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return s.one(user(j),"journal",id);}
 @PatchMapping("/journal/{id}") public Map<String,Object> editJournal(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestBody PrivateWellbeing.Writing in){return s.write(user(j),id,in);}
 @PostMapping("/journal/entries") public Map<String,Object> addJournal(@AuthenticationPrincipal Jwt j,@RequestBody PrivateWellbeing.Writing in){return s.write(user(j),null,in);}
 @PostMapping("/check-ins/entries") public Map<String,Object> addMood(@AuthenticationPrincipal Jwt j,@RequestBody PrivateWellbeing.Mood in){return s.mood(user(j),null,in);}
 @PatchMapping("/check-ins/{id}") public Map<String,Object> editMood(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestBody PrivateWellbeing.Mood in){return s.mood(user(j),id,in);}
 @DeleteMapping("/check-ins/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void deleteMood(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){s.delete(user(j),"check-ins",id);}
 @GetMapping("/check-ins/summary") public Map<String,Object> summary(@AuthenticationPrincipal Jwt j,@RequestParam String timezone,@RequestParam String from,@RequestParam String until){return s.summary(user(j),timezone,from,until);}
 @PostMapping("/goals/entries") public Map<String,Object> addGoal(@AuthenticationPrincipal Jwt j,@RequestBody PrivateWellbeing.Goal in){return s.goal(user(j),null,in);}
 @PutMapping("/goals/{id}") public Map<String,Object> editGoal(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestBody PrivateWellbeing.Goal in){return s.goal(user(j),id,in);}
 @DeleteMapping("/goals/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void deleteGoal(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){s.delete(user(j),"goals",id);}
 @PostMapping("/goals/{id}/steps") public Map<String,Object> addStep(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@RequestBody PrivateWellbeing.Step in){return s.step(user(j),id,null,in,false);}
 @PutMapping("/goals/{id}/steps/{step}") public Map<String,Object> editStep(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@PathVariable UUID step,@RequestBody PrivateWellbeing.Step in){return s.step(user(j),id,step,in,false);}
 @DeleteMapping("/goals/{id}/steps/{step}") public Map<String,Object> deleteStep(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@PathVariable UUID step,@RequestParam long revision){return s.step(user(j),id,step,new PrivateWellbeing.Step("",false,revision),true);}
 @GetMapping("/daily-reflection") public Map<String,Object> daily(@AuthenticationPrincipal Jwt j,@RequestParam(defaultValue="UTC")String timezone,@RequestParam(defaultValue="en")String language){return s.daily(user(j),timezone,language);}
 @GetMapping("/daily-reflection/history") public List<Map<String,Object>> dailyHistory(@AuthenticationPrincipal Jwt j,@RequestParam(defaultValue="0")int page){return s.dailyHistory(user(j),page);}
 @PutMapping("/daily-reflection/responses") public Map<String,Object> answer(@AuthenticationPrincipal Jwt j,@RequestBody PrivateWellbeing.Answer in){return s.answer(user(j),in,false);}
 @DeleteMapping("/daily-reflection/responses/{setId}/{position}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void deleteAnswer(@AuthenticationPrincipal Jwt j,@PathVariable UUID setId,@PathVariable int position,@RequestParam long revision){s.answer(user(j),new PrivateWellbeing.Answer(setId,position,"",revision),true);}
 @GetMapping("/journal-draft") public Map<String,Object> draft(@AuthenticationPrincipal Jwt j){return s.draft(user(j),null,false);}
 @PutMapping("/journal-draft") public Map<String,Object> saveDraft(@AuthenticationPrincipal Jwt j,@RequestBody PrivateWellbeing.Writing in){return s.draft(user(j),in,false);}
 @DeleteMapping("/journal-draft") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void discardDraft(@AuthenticationPrincipal Jwt j){s.draft(user(j),null,true);}
}
