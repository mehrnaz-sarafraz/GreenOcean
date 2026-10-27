package com.greenocean.backend.preference.service;

import com.greenocean.backend.common.exception.*;
import com.greenocean.backend.identity.AccessPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** Purpose records contain fixed classifications only, never topic selections or free health text. */
@Service
public class PrivacyGovernanceService {
 private final JdbcTemplate db;private final AccessPolicy access;
 private final boolean optionalApproved,topicsEnabled,intakeEnabled;private final String rightsOwner;private final int responseHours;
 private static final Map<String,String> NOTICES=Map.of("SUPPORT_TOPICS","support-topics-processing","OPTIONAL_ANALYTICS","optional-analytics","OPTIONAL_COMMUNICATIONS","optional-communications");
 public PrivacyGovernanceService(JdbcTemplate db,AccessPolicy access,
  @Value("${app.governance.optional-processing-approved:false}") boolean approved,
  @Value("${app.privacy.support-topics-enabled:false}") boolean topics,
  @Value("${app.governance.rights-intake-enabled:false}") boolean intake,
  @Value("${app.governance.rights-owner:}") String owner,
  @Value("${app.governance.rights-response-hours:0}") int hours) {
  this.db=db;this.access=access;optionalApproved=approved;topicsEnabled=topics;intakeEnabled=intake;rightsOwner=owner;responseHours=hours;
  if(intake&&(owner.isBlank()||hours<1||hours>8760))throw new IllegalArgumentException("Rights intake requires an actual owner and bounded operational response target");
 }
 private void lock(UUID user){db.update("INSERT INTO user_preferences(user_id) VALUES (?) ON CONFLICT DO NOTHING",user);db.queryForList("SELECT user_id FROM user_preferences WHERE user_id=? FOR UPDATE",user);}
 public Optional<UUID> approvedNotice(String purpose){
  if(!NOTICES.containsKey(purpose))throw new IllegalArgumentException("Unsupported processing purpose");
  if(!optionalApproved||("SUPPORT_TOPICS".equals(purpose)&&!topicsEnabled))return Optional.empty();
  // Analytics/marketing have no approved processing pipeline in Phase J. Never make a decorative grant.
  if(!"SUPPORT_TOPICS".equals(purpose))return Optional.empty();
  return db.query("SELECT id FROM policy_documents WHERE policy_key=? AND status='PUBLISHED' AND review_status='APPROVED' AND locale='en' AND published_at<=CURRENT_TIMESTAMP AND effective_at<=CURRENT_TIMESTAMP AND content IS NOT NULL ORDER BY version DESC LIMIT 1",(rs,n)->rs.getObject("id",UUID.class),NOTICES.get(purpose)).stream().findFirst();
 }
 @Transactional(readOnly=true) public Map<String,Object> snapshot(UUID user){return snapshot(user,false);}
 @Transactional(readOnly=true) public Map<String,Object> archive(UUID user){return snapshot(user,true);}
 private Map<String,Object> snapshot(UUID user,boolean complete){
  var choices=new ArrayList<Map<String,Object>>();
  for(String purpose:List.of("SUPPORT_TOPICS","OPTIONAL_ANALYTICS","OPTIONAL_COMMUNICATIONS")){
   var event=db.queryForList("SELECT decision,notice_id,created_at FROM processing_authorizations WHERE user_id=? AND purpose=? ORDER BY event_order DESC LIMIT 1",user,purpose);
   var currentNotice=approvedNotice(purpose);
   var choice=new LinkedHashMap<String,Object>();choice.put("currentNoticeGranted",!event.isEmpty()&&"GRANTED".equals(event.getFirst().get("decision"))&&currentNotice.filter(id->id.equals(event.getFirst().get("notice_id"))).isPresent());choice.put("purpose",purpose);choice.put("decision",event.isEmpty()?"UNKNOWN":event.getFirst().get("decision"));choice.put("grantAvailable",currentNotice.isPresent());choice.put("noticeId",currentNotice.orElse(null));choice.put("updatedAt",event.isEmpty()?null:event.getFirst().get("created_at"));choices.add(choice);
  }
  var result=new LinkedHashMap<String,Object>();result.put("choices",choices);result.put("manualIntakeAvailable",intakeEnabled);result.put("responseTargetHours",intakeEnabled?responseHours:null);
  result.put("noticeReceipts",db.queryForList("SELECT r.notice_id,r.received_at,p.policy_key,p.version,p.locale FROM privacy_notice_receipts r JOIN policy_documents p ON p.id=r.notice_id WHERE r.user_id=? ORDER BY r.received_at DESC",user));
  result.put("authorizationHistory",db.queryForList("SELECT id,purpose,decision,notice_id,created_at FROM processing_authorizations WHERE user_id=? ORDER BY event_order DESC"+(complete?"":" LIMIT 100"),user));
  result.put("termsAcceptances",db.queryForList("SELECT policy_key,version,accepted_at FROM policy_acceptances WHERE user_id=? ORDER BY accepted_at DESC",user));
  result.put("rightsRequests",complete?db.queryForList("SELECT id,kind,status,resolution_reference,created_at,updated_at FROM data_rights_requests WHERE user_id=? ORDER BY created_at DESC",user):rights(user));return result;
 }
 @Transactional public Map<String,Object> decide(UUID user,String purpose,String decision,UUID noticeId){
  if(!NOTICES.containsKey(purpose)||!Set.of("GRANTED","DENIED","WITHDRAWN").contains(decision))throw new IllegalArgumentException("Invalid purpose decision");
  lock(user);
  if("GRANTED".equals(decision)){
   access.require(user,"COMMUNITY");
   if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM users u JOIN account_eligibility e ON e.user_id=u.id WHERE u.id=? AND u.status='ACTIVE' AND u.email_verified AND e.age_result='ADULT')",Boolean.class,user)))throw new ForbiddenException("Current verified adult eligibility is required for optional processing");
   if(!approvedNotice(purpose).filter(id->id.equals(noticeId)).isPresent())throw new ForbiddenException("This purpose has no approved available notice or processing authorization");
  }
  var previous=db.queryForList("SELECT decision,notice_id FROM processing_authorizations WHERE user_id=? AND purpose=? ORDER BY event_order DESC LIMIT 1",user,purpose);
  UUID storedNotice="GRANTED".equals(decision)?noticeId:null;
  if(previous.isEmpty()||!decision.equals(previous.getFirst().get("decision"))||!Objects.equals(storedNotice,previous.getFirst().get("notice_id"))){
   int recent=db.queryForObject("SELECT count(*) FROM processing_authorizations WHERE user_id=? AND created_at>CURRENT_TIMESTAMP-INTERVAL '1 day'",Integer.class,user);
   // Withdrawal must remain possible even when a client exhausts the voluntary-event budget.
   if(recent>=24&&!"WITHDRAWN".equals(decision))throw new TooManyRequestsException("Too many preference changes; try later",3600);
   db.update("INSERT INTO processing_authorizations(user_id,purpose,decision,notice_id) VALUES (?,?,?,?)",user,purpose,decision,storedNotice);
  }
  if("SUPPORT_TOPICS".equals(purpose)&&!"GRANTED".equals(decision))db.update("UPDATE user_preferences SET support_topics='{}',legacy_support_topics='{}' WHERE user_id=?",user);
  return snapshot(user);
 }
 @Transactional public void receiveNotice(UUID user,UUID noticeId){
  if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM policy_documents WHERE id=? AND policy_key='privacy' AND status='PUBLISHED' AND published_at<=CURRENT_TIMESTAMP AND (effective_at IS NULL OR effective_at<=CURRENT_TIMESTAMP))",Boolean.class,noticeId)))throw new NotFoundException("Current Privacy Notice was not found");
  db.update("INSERT INTO privacy_notice_receipts(user_id,notice_id) VALUES (?,?) ON CONFLICT DO NOTHING",user,noticeId);
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> rights(UUID user){return db.queryForList("SELECT id,kind,status,resolution_reference,created_at,updated_at FROM data_rights_requests WHERE user_id=? ORDER BY created_at DESC LIMIT 100",user);}
 @Transactional public Map<String,Object> requestRight(UUID user,String kind,UUID retryKey){
  if(!intakeEnabled)throw new ForbiddenException("Manual rights intake is not staffed yet; existing account export, correction and deletion remain available");
  if(!Set.of("ACCESS","RECTIFICATION","ERASURE","RESTRICTION","OBJECTION","PORTABILITY","HUMAN_REVIEW").contains(kind)||retryKey==null)throw new IllegalArgumentException("Invalid rights request");
  lock(user);var existing=db.queryForList("SELECT id,kind,status,resolution_reference,created_at,updated_at FROM data_rights_requests WHERE user_id=? AND idempotency_key=?",user,retryKey);
  if(!existing.isEmpty()){if(!kind.equals(existing.getFirst().get("kind")))throw new ConflictException("Retry key belongs to a different request");return existing.getFirst();}
  if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM data_rights_requests WHERE user_id=? AND kind=? AND status IN ('RECEIVED','IN_REVIEW','NEEDS_INFORMATION'))",Boolean.class,user,kind)))throw new ConflictException("An open request of this kind already exists");
  if(db.queryForObject("SELECT count(*) FROM data_rights_requests WHERE user_id=? AND created_at>CURRENT_TIMESTAMP-INTERVAL '1 day'",Integer.class,user)>=5)throw new TooManyRequestsException("Too many rights requests",3600);
  UUID id=UUID.randomUUID();db.update("INSERT INTO data_rights_requests(id,user_id,kind,idempotency_key) VALUES (?,?,?,?)",id,user,kind,retryKey);
  return rights(user).stream().filter(r->r.get("id").equals(id)).findFirst().orElseThrow();
 }
}
