package com.greenocean.backend.auth.service;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
/** Fixed classifications only: no secrets, addresses, IPs, device labels or private content. Reuses audit_logs. */
@Service
public class SecurityEvents {
 public enum Event { LOGIN_SUCCESS,LOGIN_FAILURE,AUTH_RATE_LIMITED,RESET_REQUESTED,RESET_COMPLETED,PASSWORD_CHANGED,SESSION_REVOKED,LOGOUT,LOGOUT_ALL,REFRESH_REPLAY,REAUTH_SUCCESS,REAUTH_FAILURE,MFA_REGISTERED,MFA_REMOVED,MFA_SUCCESS,MFA_FAILURE,PRIVILEGED_DENIED,SESSION_EXPIRED,ACCOUNT_DISABLED }
 private final JdbcTemplate db;private final int retentionDays;
 private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(SecurityEvents.class);
 public SecurityEvents(JdbcTemplate db,@Value("${greenocean.security.events-retention-days:30}") int retention){if(retention<1||retention>365)throw new IllegalArgumentException("Invalid security retention");this.db=db;this.retentionDays=retention;}
 public void record(UUID user,UUID session,Event event){
  // Bounded per actor/hour, including anonymous failures; only fixed enum enters SQL/logs.
  db.update("INSERT INTO audit_logs(actor_user_id,action,entity_type,entity_id,metadata) SELECT ?,?,'SECURITY',?,'{}' WHERE (SELECT count(*) FROM audit_logs WHERE entity_type='SECURITY' AND actor_user_id IS NOT DISTINCT FROM CAST(? AS UUID) AND created_at>CURRENT_TIMESTAMP-INTERVAL '1 hour')<200",user,event.name(),session,user);
  log.info("security_event kind={}",event.name());
 }
 public java.util.List<java.util.Map<String,Object>> mine(UUID user){return db.queryForList("SELECT action,created_at FROM audit_logs WHERE actor_user_id=? AND entity_type='SECURITY' ORDER BY created_at DESC LIMIT 50",user);}
 @Scheduled(fixedDelayString="${greenocean.security.events-poll-ms:3600000}") public void retain(){
  db.update("DELETE FROM audit_logs WHERE id IN (SELECT id FROM audit_logs WHERE entity_type='SECURITY' AND created_at<CURRENT_TIMESTAMP-(? * INTERVAL '1 day') LIMIT 1000)",retentionDays);
  db.update("DELETE FROM webauthn_ceremonies WHERE expires_at<CURRENT_TIMESTAMP-INTERVAL '1 day'");
 }
}
