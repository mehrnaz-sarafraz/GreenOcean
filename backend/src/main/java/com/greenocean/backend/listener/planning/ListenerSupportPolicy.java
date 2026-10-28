package com.greenocean.backend.listener.planning;

import com.greenocean.backend.listener.live.*;
import com.greenocean.backend.auth.service.AuthRateLimit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service
public class ListenerSupportPolicy {
 public final JdbcTemplate db;
 private final LiveSupportSettings live;
 private final AuthRateLimit rates;
 private final boolean schedules,bookings,queue,followup,durations;
 public record Limits(int plannedSeconds,int extensionSeconds,int queueSeconds,int upcomingLimit,int horizonDays,int leadSeconds,int checkinSeconds,int graceSeconds,int matchSeconds,int bufferSeconds,int handoffSeconds,int queueCapacity,int maxRules,int maxExceptions){
  public int slotStepSeconds(){return ((plannedSeconds+bufferSeconds+handoffSeconds+299)/300)*300;}
 }
 public ListenerSupportPolicy(JdbcTemplate db,LiveSupportSettings live,AuthRateLimit rates,
  @Value("${greenocean.listener-support.scheduling-enabled:false}")boolean schedules,
  @Value("${greenocean.listener-support.booking-enabled:false}")boolean bookings,
  @Value("${greenocean.listener-support.queue-enabled:false}")boolean queue,
  @Value("${greenocean.listener-support.followup-enabled:false}")boolean followup,
  @Value("${greenocean.listener-support.duration-enabled:false}")boolean durations){this.db=db;this.live=live;this.rates=rates;this.schedules=schedules;this.bookings=bookings;this.queue=queue;this.followup=followup;this.durations=durations;}
 public Limits limits(){return db.queryForObject("SELECT * FROM listener_support_policy",(r,n)->new Limits(r.getInt("planned_seconds"),r.getInt("extension_seconds"),r.getInt("queue_seconds"),r.getInt("upcoming_limit"),r.getInt("horizon_days"),r.getInt("lead_seconds"),r.getInt("checkin_seconds"),r.getInt("grace_seconds"),r.getInt("match_seconds"),r.getInt("buffer_seconds"),r.getInt("handoff_seconds"),r.getInt("queue_capacity"),r.getInt("max_rules"),r.getInt("max_exceptions")));}
 public Instant now(){return db.queryForObject("SELECT listener_support_now()",OffsetDateTime.class).toInstant();}
 public static Instant instant(Object value){return value==null?null:value instanceof OffsetDateTime t?t.toInstant():((java.sql.Timestamp)value).toInstant();}
 public static OffsetDateTime date(Instant value){return value.atOffset(ZoneOffset.UTC);}
 public static LiveSupportException denied(int status,String code,String text){return new LiveSupportException(status,code,text);}
 public boolean planning(){return live.enabled()&&durations;}
 public boolean scheduling(){return planning()&&schedules;}
 public boolean booking(){return scheduling()&&bookings;}
 public boolean queue(){return planning()&&queue;}
 public boolean followup(){return planning()&&followup;}
 public void require(String mode){boolean enabled=switch(mode){case "SCHEDULE"->scheduling();case "BOOKING"->booking();case "QUEUE"->queue();case "FOLLOWUP"->followup();default->planning();};if(!enabled)throw denied(503,"SUPPORT_PATH_DISABLED","This support pathway is not enabled. Other support alternatives remain available.");if(limits().handoffSeconds()<live.offerSeconds()+live.activationSeconds())throw denied(503,"SUPPORT_POLICY_CONFLICT","Support capacity configuration requires operator review.");}
 public void eligible(UUID who){if(!truth("SELECT live_account_eligible(?)",who))throw denied(403,"SERVICE_FORBIDDEN","Current listener-service account and regional eligibility are required.");}
 public void listener(UUID who){eligible(who);if(!truth("SELECT live_listener_qualified(?)",who))throw denied(403,"LISTENER_NOT_AUTHORIZED","Independent current listener qualification is required.");}
 public boolean truth(String sql,Object...args){return Boolean.TRUE.equals(db.queryForObject(sql,Boolean.class,args));}
 public void lock(UUID a,UUID b){db.queryForList("SELECT listener_lock_pair(?,?)",a,b);}
 public void liveLock(){db.queryForList("SELECT pg_advisory_xact_lock(72491001)");}
 public void bound(UUID who,String action,int count){if(!rates.allowInCurrentTransaction("listener-planning:"+action+":"+who,count,Duration.ofHours(1)))throw denied(429,"RATE_LIMITED","Please pause before trying this support action again.");}
 public void shutdownDisabled(){if(!queue()){
  db.update("UPDATE listener_wait_queue SET status='SERVICE_UNAVAILABLE',reason_code='SERVICE_DISABLED' WHERE status IN('WAITING','OFFERED')");
 }if(!booking()){
  db.update("UPDATE live_support_requests SET status='CANCELLED',reason_code='SERVICE_DISABLED',ended_at=clock_timestamp() WHERE source_booking_id IS NOT NULL AND status IN('REQUESTED','ACCEPTED')");
  db.update("UPDATE listener_bookings SET status='CANCELLED_BY_SERVICE',reason_code='SERVICE_DISABLED',revision=revision+1 WHERE status IN('CONFIRMED','CHECKED_IN','HANDOFF_REQUESTED')");}}
 public void notice(UUID user,UUID entity,String kind,boolean optedIn){if(!optedIn||!truth("SELECT COALESCE((SELECT notify_messages FROM user_preferences WHERE user_id=?),FALSE)",user))return;
  if(db.update("INSERT INTO listener_support_notices(user_id,entity_id,kind) VALUES(?,?,?) ON CONFLICT DO NOTHING",user,entity,kind)==1)db.update("INSERT INTO notifications(user_id,type,title,body) VALUES(?,'SYSTEM_NOTICE','SUPPORT_UPDATE','Open GreenOcean to review an update.')",user);
 }
 public Map<String,Object> options(){return Map.of("schedulingEnabled",scheduling(),"bookingEnabled",booking(),"queueEnabled",queue(),"followupEnabled",followup(),"durationEnabled",planning(),"serverTime",now(),"limits",limits());}
}
