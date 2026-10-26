package com.greenocean.backend.auth.service;
import com.yubico.webauthn.*;import com.yubico.webauthn.data.*;
import com.greenocean.backend.auth.repository.UserRepository;import com.greenocean.backend.common.exception.*;
import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import org.springframework.jdbc.core.JdbcTemplate;
import java.time.*;import java.net.URI;import java.util.*;
@Service public class PrivilegedWebAuthn {
 private final JdbcTemplate db;private final UserRepository users;private final RecentAuthentication recent;private final AuthRateLimit limits;private final SecurityEvents events;private final TokenService tokens;private final RelyingParty rp;private final Clock clock;private final AuthenticationService auth;
 public PrivilegedWebAuthn(JdbcTemplate db,UserRepository users,RecentAuthentication recent,AuthRateLimit limits,SecurityEvents events,TokenService tokens,Clock clock,WebAuthnCredentials credentials,AuthenticationService auth,
 @Value("${greenocean.webauthn.enabled:false}") boolean enabled,@Value("${greenocean.webauthn.rp-id:}") String rpId,@Value("${greenocean.webauthn.origins:}") String origins){
  this.db=db;this.users=users;this.recent=recent;this.limits=limits;this.events=events;this.tokens=tokens;this.clock=clock;this.auth=auth;
  if(!enabled){rp=null;return;}
  Set<String> trusted=new HashSet<>();
  if(!rpId.matches("[A-Za-z0-9.-]+")||origins.isBlank())throw new IllegalStateException("WebAuthn requires an explicit RP and HTTPS origins");
  for(String origin:origins.split(",")){var uri=URI.create(origin.trim());if(!"https".equals(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null||uri.getQuery()!=null||uri.getFragment()!=null||!(uri.getPath().isEmpty()||"/".equals(uri.getPath()))||!(uri.getHost().equals(rpId)||uri.getHost().endsWith("."+rpId)))throw new IllegalStateException("Invalid WebAuthn origin");trusted.add(origin.trim());}
  rp=RelyingParty.builder().identity(RelyingPartyIdentity.builder().id(rpId).name("GreenOcean staff security").build()).credentialRepository(credentials).origins(trusted).allowOriginPort(false).allowOriginSubdomain(false).build();
 }
 public boolean available(){return rp!=null;}
 private void owner(UUID user,UUID session){
  if(rp==null)throw new ForbiddenException("Security key access is not configured. Staff access remains disabled.");
  var account=users.lockById(user).orElseThrow(()->new ForbiddenException("Security key access unavailable"));
  if(account.getStatus()!=com.greenocean.backend.auth.entity.UserStatus.ACTIVE||!account.isEmailVerified()||!recent.privileged(user))throw new ForbiddenException("Security key access unavailable");
  if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM user_sessions WHERE id=? AND user_id=? AND session_scope='PRIVILEGED')",Boolean.class,session,user)))throw new ForbiddenException("Sign in again before staff authentication");
  recent.require(user,session,false);
 }
 private void budget(UUID user){if(!limits.allow("webauthn:"+user,10,Duration.ofMinutes(15)))throw new ForbiddenException("Wait before trying the security key again");}
 public record Start(UUID ceremonyId,String optionsJson){}
 private Start save(UUID user,UUID session,String purpose,String stored,String options){UUID id=UUID.randomUUID();db.update("UPDATE webauthn_ceremonies SET used_at=CURRENT_TIMESTAMP WHERE user_id=? AND session_id=? AND used_at IS NULL",user,session);db.update("INSERT INTO webauthn_ceremonies(id,user_id,session_id,purpose,request_json,expires_at) VALUES (?,?,?,?,?,?)",id,user,session,purpose,stored,java.sql.Timestamp.from(clock.instant().plusSeconds(300)));return new Start(id,options);}
 @Transactional public Start register(UUID user,UUID session,String permit)throws Exception{
  budget(user);owner(user,session);
  int count=db.queryForObject("SELECT count(*) FROM webauthn_credentials WHERE user_id=? AND revoked_at IS NULL",Integer.class,user);
  if(count>=5)throw new ForbiddenException("At most five security keys may be registered");
  if(count==0){int claimed=db.update("UPDATE webauthn_enrollment_permits SET used_at=CURRENT_TIMESTAMP WHERE user_id=? AND token_hash=? AND used_at IS NULL AND expires_at>CURRENT_TIMESTAMP",user,tokens.hashRefreshToken(permit==null?"":permit));if(claimed!=1)throw new ForbiddenException("An independently authorized enrollment permit is required");}
  else recent.require(user,session,true);
  var request=rp.startRegistration(StartRegistrationOptions.builder().user(UserIdentity.builder().name(user.toString()).displayName("GreenOcean staff").id(WebAuthnCredentials.handle(user)).build()).authenticatorSelection(AuthenticatorSelectionCriteria.builder().userVerification(UserVerificationRequirement.REQUIRED).build()).timeout(300000).build());
  return save(user,session,"REGISTER",request.toJson(),request.toCredentialsCreateJson());
 }
 @Transactional public Start authenticate(UUID user,UUID session)throws Exception{
  budget(user);owner(user,session);
  var request=rp.startAssertion(StartAssertionOptions.builder().username(user.toString()).userVerification(UserVerificationRequirement.REQUIRED).timeout(300000).build());
  return save(user,session,"AUTHENTICATE",request.toJson(),request.toCredentialsGetJson());
 }
 private String consume(UUID user,UUID session,UUID ceremony,String purpose){var rows=db.queryForList("UPDATE webauthn_ceremonies SET used_at=CURRENT_TIMESTAMP WHERE id=? AND user_id=? AND session_id=? AND purpose=? AND used_at IS NULL AND expires_at>? RETURNING request_json",String.class,ceremony,user,session,purpose,java.sql.Timestamp.from(clock.instant()));if(rows.isEmpty())throw new ForbiddenException("Security key challenge is invalid or expired");return rows.getFirst();}
 @Transactional(noRollbackFor=ForbiddenException.class) public void finishRegistration(UUID user,UUID session,UUID ceremony,String response,String label){
  budget(user);owner(user,session);String request=consume(user,session,ceremony,"REGISTER");
  try{var result=rp.finishRegistration(FinishRegistrationOptions.builder().request(PublicKeyCredentialCreationOptions.fromJson(request)).response(PublicKeyCredential.parseRegistrationResponseJson(response)).build());if(!result.isUserVerified())throw new IllegalArgumentException();
   String safe=label==null?"Security key":label.replaceAll("[\\p{Cc}\\p{Cf}]", "").trim();if(safe.isEmpty()||safe.length()>80)safe="Security key";
   db.update("INSERT INTO webauthn_credentials(credential_id,user_id,public_key_cose,signature_count,label) VALUES (?,?,?,?,?)",result.getKeyId().getId().getBase64Url(),user,result.getPublicKeyCose().getBase64Url(),result.getSignatureCount(),safe);events.record(user,session,SecurityEvents.Event.MFA_REGISTERED);
  }catch(Exception failure){events.record(user,session,SecurityEvents.Event.MFA_FAILURE);throw new ForbiddenException("Security key registration failed");}
 }
 @Transactional(noRollbackFor=ForbiddenException.class) public void finishAuthentication(UUID user,UUID session,UUID ceremony,String response){
  budget(user);owner(user,session);String request=consume(user,session,ceremony,"AUTHENTICATE");
  try{var result=rp.finishAssertion(FinishAssertionOptions.builder().request(AssertionRequest.fromJson(request)).response(PublicKeyCredential.parseAssertionResponseJson(response)).build());if(!result.isSuccess()||!result.isUserVerified()||!user.toString().equals(result.getUsername()))throw new IllegalArgumentException();
   int changed=db.update("UPDATE webauthn_credentials SET signature_count=? WHERE credential_id=? AND user_id=? AND revoked_at IS NULL",result.getSignatureCount(),result.getCredentialId().getBase64Url(),user);if(changed!=1)throw new IllegalArgumentException();
   int active=db.update("UPDATE user_sessions SET token_version=token_version+1,mfa_authenticated_at=?,reauthenticated_at=? WHERE id=? AND user_id=? AND revoked_at IS NULL AND overall_expires_at>? AND idle_expires_at>?",java.sql.Timestamp.from(clock.instant()),java.sql.Timestamp.from(clock.instant()),session,user,java.sql.Timestamp.from(clock.instant()),java.sql.Timestamp.from(clock.instant()));if(active!=1)throw new IllegalArgumentException();events.record(user,session,SecurityEvents.Event.MFA_SUCCESS);
  }catch(Exception failure){events.record(user,session,SecurityEvents.Event.MFA_FAILURE);throw new ForbiddenException("Security key verification failed");}
 }
 @Transactional(noRollbackFor=ForbiddenException.class) public com.greenocean.backend.auth.dto.TokenResponse completeAuthentication(UUID user,UUID session,UUID ceremony,String response,String refresh){
  if(refresh==null||!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM user_sessions WHERE id=? AND user_id=? AND refresh_token_hash=? AND revoked_at IS NULL)",Boolean.class,session,user,tokens.hashRefreshToken(refresh))))throw new ForbiddenException("Use the current session to verify the security key");
  finishAuthentication(user,session,ceremony,response);
  return auth.refresh(refresh);
 }
 public List<Map<String,Object>> credentials(UUID user){return db.queryForList("SELECT credential_id,label,created_at FROM webauthn_credentials WHERE user_id=? AND revoked_at IS NULL ORDER BY created_at",user);}
 @Transactional public void remove(UUID user,UUID session,String credential){owner(user,session);recent.require(user,session,true);int count=db.queryForObject("SELECT count(*) FROM webauthn_credentials WHERE user_id=? AND revoked_at IS NULL",Integer.class,user);if(count<2)throw new ForbiddenException("Register and verify a replacement key before removing the last key");if(db.update("UPDATE webauthn_credentials SET revoked_at=CURRENT_TIMESTAMP WHERE credential_id=? AND user_id=? AND revoked_at IS NULL",credential,user)!=1)throw new NotFoundException("Security key was not found");db.update("UPDATE user_sessions SET revoked_at=CURRENT_TIMESTAMP WHERE user_id=?",user);events.record(user,session,SecurityEvents.Event.MFA_REMOVED);}
}
