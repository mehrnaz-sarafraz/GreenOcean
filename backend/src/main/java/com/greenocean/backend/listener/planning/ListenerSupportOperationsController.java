package com.greenocean.backend.listener.planning;
import org.springframework.web.bind.annotation.*;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import java.util.*;
@RestController @RequestMapping("/api/v1/admin/listener-support") @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
public class ListenerSupportOperationsController {
 private final ListenerSupportOperations operations;public ListenerSupportOperationsController(ListenerSupportOperations o){operations=o;}
 @GetMapping("/operations") public Map<String,Object> dashboard(@AuthenticationPrincipal Jwt j){return operations.dashboard(UUID.fromString(j.getSubject()));}
 @GetMapping("/qualified-listeners") public List<Map<String,Object>> listeners(@AuthenticationPrincipal Jwt j,@RequestParam(required=false)UUID after){return operations.listeners(UUID.fromString(j.getSubject()),after);}
}
