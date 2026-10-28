package com.greenocean.backend.listener.planning;
import jakarta.validation.Valid;import org.springframework.web.bind.annotation.*;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import java.util.*;
@RestController @RequestMapping("/api/v1/listener-support/queue")
public class ListenerQueueController {
 private final ListenerQueue queue;public ListenerQueueController(ListenerQueue queue){this.queue=queue;}
 private UUID who(Jwt jwt){return UUID.fromString(jwt.getSubject());}
 @GetMapping("/me") public Map<String,Object> state(@AuthenticationPrincipal Jwt jwt){return queue.state(who(jwt));}
 @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public Map<String,Object> join(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody ListenerQueue.Admission input){return queue.join(who(jwt),input);}
 @GetMapping("/{id}") public Map<String,Object> get(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return queue.get(id,who(jwt));}
 @PostMapping("/{id}/cancel") public Map<String,Object> cancel(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return queue.cancel(id,who(jwt));}
 @PostMapping("/{id}/confirm") public Map<String,Object> confirm(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody ListenerQueue.Confirmation input){return queue.confirm(id,who(jwt),input);}
}
