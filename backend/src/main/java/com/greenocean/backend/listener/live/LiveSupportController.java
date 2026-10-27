package com.greenocean.backend.listener.live;
import com.greenocean.backend.listener.dto.ListenerResponse;
import com.greenocean.backend.listener.service.ListenerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class LiveSupportController {
 private final LiveSupportService service;private final ListenerService listeners;
 public LiveSupportController(LiveSupportService service,ListenerService listeners){this.service=service;this.listeners=listeners;}
 public record Create(@NotNull UUID clientRequestId,UUID listenerId,@Null String topic){}
 public record Ready(boolean ready,UUID leaseId){}
 public record Beat(@NotNull UUID leaseId){}
 private UUID user(Jwt jwt){return UUID.fromString(jwt.getSubject());}
 private UUID session(Jwt jwt){return UUID.fromString(jwt.getClaimAsString("sid"));}
 private int version(Jwt jwt){return ((Number)jwt.getClaim("sv")).intValue();}
 @GetMapping("/listener-support/availability") public Map<String,Object> discovery(@AuthenticationPrincipal Jwt jwt){
  service.reconcile();return Map.of("enabled",service.enabled(),"offerSeconds",service.offerSeconds(),"listeners",service.enabled()?listeners.listeners(user(jwt),true,null):List.<ListenerResponse>of());
 }
 @GetMapping("/listeners/me/availability") public ListenerAvailability availability(@AuthenticationPrincipal Jwt jwt){return service.availability(user(jwt),session(jwt));}
 @PutMapping("/listeners/me/availability") public ListenerAvailability ready(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Ready input){return service.setAvailability(user(jwt),session(jwt),version(jwt),input.ready(),input.leaseId());}
 @PostMapping("/listeners/me/heartbeat") public ListenerAvailability beat(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Beat input){return service.heartbeat(user(jwt),session(jwt),version(jwt),input.leaseId());}
 @PostMapping("/listener-support/requests") public LiveSupportView create(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Create input){return service.create(user(jwt),input.clientRequestId(),input.listenerId());}
 @GetMapping("/listener-support/requests/me") public List<LiveSupportView> mine(@AuthenticationPrincipal Jwt jwt){return service.mine(user(jwt));}
 @GetMapping("/listener-support/incoming") public List<LiveSupportView> incoming(@AuthenticationPrincipal Jwt jwt){return service.incoming(user(jwt));}
 @GetMapping("/listener-support/requests/{id}") public LiveSupportView get(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return service.get(id,user(jwt));}
 @PostMapping("/listener-support/requests/{id}/accept") public LiveSupportView accept(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return service.accept(id,user(jwt));}
 @PostMapping("/listener-support/requests/{id}/decline") public LiveSupportView decline(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return service.decline(id,user(jwt));}
 @PostMapping("/listener-support/requests/{id}/cancel") public LiveSupportView cancel(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return service.cancel(id,user(jwt));}
 @PostMapping("/listener-support/requests/{id}/activate") public LiveSupportView activate(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return service.activate(id,user(jwt));}
 @PostMapping("/listener-support/requests/{id}/heartbeat") public LiveSupportView pulse(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return service.pulse(id,user(jwt));}
 @PostMapping("/listener-support/sessions/{id}/end") public LiveSupportView end(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return service.end(id,user(jwt));}
 @GetMapping("/listener-support/sessions/{id}") public LiveSupportView session(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return service.get(id,user(jwt));}
 @GetMapping("/listener-support/conversations/{id}") public LiveSupportView conversation(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return service.forConversation(id,user(jwt));}
}
