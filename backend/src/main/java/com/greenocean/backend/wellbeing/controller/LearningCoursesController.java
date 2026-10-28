package com.greenocean.backend.wellbeing.controller;
import com.greenocean.backend.wellbeing.service.LearningCourses;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.*;
@RestController @RequestMapping("/api/v1/wellbeing")
public class LearningCoursesController {
 private final LearningCourses s;public LearningCoursesController(LearningCourses s){this.s=s;}private UUID user(Jwt j){return UUID.fromString(j.getSubject());}
 @GetMapping("/courses") public Map<String,Object> browse(@AuthenticationPrincipal Jwt j,@RequestParam(defaultValue="")String search,@RequestParam(defaultValue="")String language,@RequestParam(defaultValue="0")int page){return s.browse(user(j),search,language,page);}
 @GetMapping("/courses/{id}") public Map<String,Object> course(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return s.course(user(j),id);}
 @GetMapping("/courses/{id}/progress") public Map<String,Object> progress(@AuthenticationPrincipal Jwt j,@PathVariable UUID id){return s.progress(user(j),id);}
 @PostMapping("/courses/{id}/lessons/{lesson}/open") public Map<String,Object> open(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@PathVariable UUID lesson){return s.open(user(j),id,lesson);}
 @PostMapping("/courses/{id}/lessons/{lesson}/complete") public Map<String,Object> complete(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@PathVariable UUID lesson,@RequestBody LearningCourses.Completion in){return s.complete(user(j),id,lesson,in);}
 @PostMapping("/courses/{id}/lessons/{lesson}/check") public Map<String,Object> check(@AuthenticationPrincipal Jwt j,@PathVariable UUID id,@PathVariable UUID lesson,@RequestBody LearningCourses.Check in){return s.check(user(j),id,lesson,in);}
 @GetMapping("/learning-history") public Map<String,Object> history(@AuthenticationPrincipal Jwt j,@RequestParam(defaultValue="0")int page){return s.history(user(j),page);}
}
