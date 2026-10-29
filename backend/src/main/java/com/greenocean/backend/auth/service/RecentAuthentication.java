package com.greenocean.backend.auth.service;
import java.time.*;import java.util.*;
import org.springframework.stereotype.Service;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.transaction.annotation.Transactional;import org.springframework.security.crypto.password.PasswordEncoder;
import com.greenocean.backend.auth.repository.UserRepository;import com.greenocean.backend.common.exception.*;
@Service public class RecentAuthentication {
 private final JdbcTemplate db;private final UserRepository users;private final PasswordEncoder passwords;private final AuthRateLimit limits;private final SessionPolicy policy;private final Clock clock;private final SecurityEvents events;private final AuthenticationService auth;private final TokenService tokens;
 public RecentAuthentication(JdbcTemplate db,UserRepository users,PasswordEncoder passwords,AuthRateLimit limits,SessionPolicy policy,Clock clock,SecurityEvents events,AuthenticationService auth,TokenService tokens){this.db=db;this.users=users;this.passwords=passwords;this.limits=limits;this.policy=policy;this.clock=clock;this.events=events;this.auth=auth;this.tokens=tokens;}
 public void require(UUID user,UUID session,boolean strong){
  var rows=db.queryForList("SELECT reauthenticated_at,mfa_authenticated_at FROM user_sessions WHERE id=? AND user_id=? AND revoked_at IS NULL AND expires_at>? AND overall_expires_at>? AND idle_expires_at>?",session,user,java.sql.Timestamp.from(clock.instant()),java.sql.Timestamp.from(clock.instant()),java.sql.Timestamp.from(clock.instant()));
  if(rows.isEmpty())throw new UnauthorizedException("Sign in again to continue");
  Object value=rows.getFirst().get(strong?"mfa_authenticated_at":"reauthenticated_at");
  if(!(value instanceof java.sql.Timestamp time)||!time.toInstant().isAfter(clock.instant().minus(policy.recent())))throw new ForbiddenException(strong?"Verify a security key again to continue":"Confirm your password again to continue");
 }
 @Transactional(noRollbackFor=ForbiddenException.class) public void password(UUID user,UUID session,String password){
  if(!limits.allowInCurrentTransaction("reauth:"+user,5,Duration.ofMinutes(15)))throw new ForbiddenException("Wait before confirming your password again");
  var account=users.lockById(user).orElseThrow(()->new UnauthorizedException("Sign in again"));
  if(!passwords.matches(password,account.getPasswordHash())){events.record(user,session,SecurityEvents.Event.REAUTH_FAILURE);throw new ForbiddenException("Password confirmation failed");}
  int updated=db.update("UPDATE user_sessions SET reauthenticated_at=? WHERE id=? AND user_id=? AND revoked_at IS NULL AND expires_at>? AND overall_expires_at>? AND idle_expires_at>?",java.sql.Timestamp.from(clock.instant()),session,user,java.sql.Timestamp.from(clock.instant()),java.sql.Timestamp.from(clock.instant()),java.sql.Timestamp.from(clock.instant()));
  if(updated!=1)throw new UnauthorizedException("Sign in again");events.record(user,session,SecurityEvents.Event.REAUTH_SUCCESS);
 }
 @Transactional public void activity(UUID user,UUID session){
  var now=clock.instant();var rows=db.queryForList("SELECT client_type,session_scope FROM user_sessions WHERE id=? AND user_id=? AND revoked_at IS NULL AND overall_expires_at>? AND idle_expires_at>? AND expires_at>?",session,user,java.sql.Timestamp.from(now),java.sql.Timestamp.from(now),java.sql.Timestamp.from(now));
  if(rows.isEmpty())throw new UnauthorizedException("Your session has expired. Sign in again.");
  var row=rows.getFirst();var idle=SessionPolicy.earlier(now.plus(policy.idle((String)row.get("client_type"),(String)row.get("session_scope"))),now.plus(policy.overall((String)row.get("client_type"),(String)row.get("session_scope"))));
  db.update("UPDATE user_sessions SET last_activity_at=?,last_used_at=?,idle_expires_at=LEAST(overall_expires_at,?) WHERE id=? AND user_id=? AND revoked_at IS NULL AND idle_expires_at>? AND overall_expires_at>? AND last_activity_at<?",java.sql.Timestamp.from(now),java.sql.Timestamp.from(now),java.sql.Timestamp.from(idle),session,user,java.sql.Timestamp.from(now),java.sql.Timestamp.from(now),java.sql.Timestamp.from(now.minusSeconds(30)));
 }
 @Transactional(noRollbackFor=ForbiddenException.class)
 public com.greenocean.backend.auth.dto.TokenResponse completePassword(UUID user,UUID session,String password,String refresh){
  users.lockById(user).orElseThrow(()->new UnauthorizedException("Sign in again"));
  if(refresh==null||!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM user_sessions WHERE id=? AND user_id=? AND refresh_token_hash=? AND revoked_at IS NULL)",Boolean.class,session,user,tokens.hashRefreshToken(refresh))))throw new ForbiddenException("Use the current session to confirm your password");
  password(user,session,password);
  db.update("UPDATE user_sessions SET token_version=token_version+1 WHERE id=? AND user_id=?",session,user);
  // Keep the original absolute/idle boundaries; only authentication freshness changes.
  return auth.refresh(refresh);
 }
 public boolean privileged(UUID user){return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=? AND r.name IN ('ADMIN','MODERATOR','PROFESSIONAL','CLINIC_FINANCE','CLINICAL_RECORDS_OPERATOR','CLINIC_REPUTATION_OPERATOR','CLINIC_OPERATIONS','CLINIC_PRIVACY_OPERATOR'))",Boolean.class,user));}
}
