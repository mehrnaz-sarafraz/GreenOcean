package com.greenocean.backend.listener.planning;
import jakarta.validation.Valid;import org.springframework.web.bind.annotation.*;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import java.util.*;
@RestController @RequestMapping("/api/v1/listener-support/sessions/{session}")
public class ListenerSessionsController {
 private final ListenerSessions sessions;public ListenerSessionsController(ListenerSessions s){sessions=s;}
 private UUID who(Jwt j){return UUID.fromString(j.getSubject());}
 @GetMapping("/plan") public Map<String,Object> plan(@PathVariable UUID session,@AuthenticationPrincipal Jwt j){return sessions.plan(session,who(j));}
 @PostMapping("/extension") public Map<String,Object> request(@PathVariable UUID session,@AuthenticationPrincipal Jwt j,@Valid @RequestBody ListenerSessions.Proposal input){return sessions.request(session,who(j),input);}
 @PostMapping("/extensions/{proposal}/approve") public Map<String,Object> approve(@PathVariable UUID session,@PathVariable UUID proposal,@AuthenticationPrincipal Jwt j){return sessions.decide(session,proposal,who(j),true);}
 @PostMapping("/extensions/{proposal}/decline") public Map<String,Object> decline(@PathVariable UUID session,@PathVariable UUID proposal,@AuthenticationPrincipal Jwt j){return sessions.decide(session,proposal,who(j),false);}
 @GetMapping("/feedback") public Map<String,Object> feedback(@PathVariable UUID session,@AuthenticationPrincipal Jwt j){return sessions.feedback(session,who(j));}
 @PutMapping("/feedback") public Map<String,Object> save(@PathVariable UUID session,@AuthenticationPrincipal Jwt j,@Valid @RequestBody ListenerSessions.Feedback input){return sessions.saveFeedback(session,who(j),input);}
 @DeleteMapping("/feedback") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void remove(@PathVariable UUID session,@AuthenticationPrincipal Jwt j){sessions.removeFeedback(session,who(j));}
 @GetMapping("/repeat-slots") public ListenerCalendar.Slots repeat(@PathVariable UUID session,@AuthenticationPrincipal Jwt j){return sessions.repeat(session,who(j));}
}
