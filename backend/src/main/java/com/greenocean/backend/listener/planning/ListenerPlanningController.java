package com.greenocean.backend.listener.planning;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import java.util.*;
@RestController @RequestMapping("/api/v1")
public class ListenerPlanningController {
 private final ListenerSupportPolicy policy;private final ListenerCalendar calendar;private final ListenerBookings bookings;
 public ListenerPlanningController(ListenerSupportPolicy policy,ListenerCalendar calendar,ListenerBookings bookings){this.policy=policy;this.calendar=calendar;this.bookings=bookings;}
 private UUID who(Jwt jwt){return UUID.fromString(jwt.getSubject());}
 public record Revision(@Min(1)long revision){}
 @GetMapping("/listener-support/options") public Map<String,Object> options(){return policy.options();}
 @GetMapping("/listeners/me/calendar") public Map<String,Object> calendar(@AuthenticationPrincipal Jwt jwt){return calendar.mine(who(jwt));}
 @PutMapping("/listeners/me/calendar") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void settings(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody ListenerCalendar.Settings in){calendar.settings(who(jwt),in);}
 @PostMapping("/listeners/me/calendar/rules") @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public Map<String,UUID> rule(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody ListenerCalendar.Rule in){return Map.of("id",calendar.rule(who(jwt),null,in));}
 @PutMapping("/listeners/me/calendar/rules/{id}") public Map<String,UUID> update(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody ListenerCalendar.Rule in){return Map.of("id",calendar.rule(who(jwt),id,in));}
 @DeleteMapping("/listeners/me/calendar/rules/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void remove(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Revision in){calendar.remove(who(jwt),id,false,in.revision());}
 @PostMapping("/listeners/me/calendar/exceptions") @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public Map<String,UUID> exception(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody ListenerCalendar.ExceptionInput in){return Map.of("id",calendar.exception(who(jwt),in));}
 @DeleteMapping("/listeners/me/calendar/exceptions/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void removeException(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody Revision in){calendar.remove(who(jwt),id,true,in.revision());}
 @GetMapping("/listener-support/calendars") public Map<String,Object> calendars(@AuthenticationPrincipal Jwt jwt,@RequestParam(required=false)UUID after,@RequestParam(defaultValue="20")int size){if(size<1||size>50)throw new IllegalArgumentException("size must be1..50");return calendar.calendars(who(jwt),after,size);}
 @GetMapping("/listener-support/slots") public ListenerCalendar.Slots slots(@AuthenticationPrincipal Jwt jwt,@RequestParam(required=false)UUID listenerId,@RequestParam(defaultValue="20")int size,@RequestParam(required=false)String cursor){if(size<1||size>50)throw new IllegalArgumentException("size must be1..50");return calendar.slots(who(jwt),listenerId,size,cursor);}
 @GetMapping("/listener-support/bookings/me") public List<Map<String,Object>> mine(@AuthenticationPrincipal Jwt jwt,@RequestParam(defaultValue="0")int page){if(page<0||page>1000)throw new IllegalArgumentException("Invalid page");return bookings.mine(who(jwt),page);}
 @GetMapping("/listener-support/bookings/{id}") public Map<String,Object> booking(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){bookings.reconcile();return bookings.view(id,who(jwt));}
 @PostMapping("/listener-support/bookings") @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public Map<String,Object> reserve(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody ListenerBookings.Reservation in){return bookings.reserve(who(jwt),in);}
 @PostMapping("/listener-support/bookings/{id}/cancel") public Map<String,Object> cancel(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return bookings.cancel(id,who(jwt));}
 @PostMapping("/listener-support/bookings/{id}/reschedule") public Map<String,Object> reschedule(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody ListenerBookings.Reservation in){return bookings.reschedule(id,who(jwt),in);}
 @PostMapping("/listener-support/bookings/{id}/check-in") public Map<String,Object> checkin(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return bookings.checkin(id,who(jwt));}
 @PostMapping("/listener-support/bookings/{id}/handoff") public Map<String,Object> handoff(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return bookings.handoff(id,who(jwt));}
}
