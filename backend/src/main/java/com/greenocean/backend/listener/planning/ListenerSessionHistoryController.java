package com.greenocean.backend.listener.planning;
import com.greenocean.backend.listener.live.*;import org.springframework.web.bind.annotation.*;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import org.springframework.transaction.annotation.Transactional;import java.util.*;
@RestController @RequestMapping("/api/v1/listener-support/history") public class ListenerSessionHistoryController {
 private final LiveSupportService live;private final ListenerSupportPolicy policy;
 public ListenerSessionHistoryController(LiveSupportService live,ListenerSupportPolicy policy){this.live=live;this.policy=policy;}
 public record Entry(LiveSupportView session,boolean feedbackSaved){}
 @GetMapping @Transactional public List<Entry> history(@AuthenticationPrincipal Jwt jwt){UUID user=UUID.fromString(jwt.getSubject());var mine=live.mine(user);var feedback=new HashSet<>(policy.db.queryForList("SELECT f.session_id FROM listener_session_feedback f JOIN(SELECT id FROM live_support_requests WHERE member_id=? OR listener_id=? ORDER BY created_at DESC LIMIT 20)r ON r.id=f.session_id WHERE f.owner_id=?",UUID.class,user,user,user));return mine.stream().map(r->new Entry(r,!r.listenerSide()&&feedback.contains(r.id()))).toList();}
}
