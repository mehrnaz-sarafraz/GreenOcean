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
 private final JdbcTemplate db;private final int retentionDays;private final com.greenocean.backend.assistance.service.OutboxCipher cipher;
 private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(SecurityEvents.class);
 public SecurityEvents(JdbcTemplate db,@Value("${greenocean.security.events-retention-days:30}") int retention,com.greenocean.backend.assistance.service.OutboxCipher cipher){if(retention<1||retention>365)throw new IllegalArgumentException("Invalid security retention");this.db=db;this.retentionDays=retention;this.cipher=cipher;}
 @org.springframework.transaction.annotation.Transactional
 public void record(UUID user,UUID session,Event event){
  db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))","security-events:"+(user==null?"anonymous":user));
  // Bounded per actor/hour, including anonymous failures; only fixed enum enters SQL/logs.
  db.update("INSERT INTO audit_logs(actor_user_id,action,entity_type,entity_id,metadata) SELECT ?,?,'SECURITY',?,'{}' WHERE (SELECT count(*) FROM audit_logs WHERE entity_type='SECURITY' AND actor_user_id IS NOT DISTINCT FROM CAST(? AS UUID) AND created_at>CURRENT_TIMESTAMP-INTERVAL '1 hour')<200",user,event.name(),session,user);
  if(user!=null&&Set.of(Event.LOGIN_SUCCESS,Event.PASSWORD_CHANGED,Event.RESET_COMPLETED,Event.REFRESH_REPLAY,Event.LOGOUT_ALL,Event.MFA_REGISTERED,Event.MFA_REMOVED).contains(event)){
   db.update("INSERT INTO email_outbox(id,owner_user_id,recipient,template_key,payload,expires_at) SELECT uuidv7(),u.id,u.email,'security-notification',jsonb_build_object('encrypted',CAST(? AS TEXT)),CURRENT_TIMESTAMP+INTERVAL '1 day' FROM users u WHERE u.id=? AND u.status='ACTIVE' AND u.email_verified AND NOT EXISTS(SELECT 1 FROM email_outbox o WHERE o.owner_user_id=u.id AND o.template_key='security-notification' AND o.created_at>CURRENT_TIMESTAMP-INTERVAL '1 hour')",cipher.encrypt(event.name()),user);
  }
  log.info("security_event kind={}",event.name());
 }
 public java.util.List<java.util.Map<String,Object>> mine(UUID user){return db.queryForList("SELECT action,created_at FROM audit_logs WHERE actor_user_id=? AND entity_type='SECURITY' ORDER BY created_at DESC LIMIT 50",user);}
 @Scheduled(fixedDelayString="${greenocean.security.events-poll-ms:3600000}") public void retain(){
  db.update("DELETE FROM audit_logs WHERE id IN (SELECT id FROM audit_logs WHERE entity_type='SECURITY' AND created_at<CURRENT_TIMESTAMP-(? * INTERVAL '1 day') LIMIT 1000)",retentionDays);
  db.update("DELETE FROM webauthn_ceremonies WHERE expires_at<CURRENT_TIMESTAMP-INTERVAL '1 day'");
 }
}
