package com.greenocean.backend.preference.service;
import com.greenocean.backend.common.exception.*;
import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
@Service
public class PolicyPublicationService {
 private final JdbcTemplate db;private final boolean enabled;
 public PolicyPublicationService(JdbcTemplate db,@Value("${app.governance.policy-publication-enabled:false}") boolean enabled){this.db=db;this.enabled=enabled;}
 private void staff(UUID actor){if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM users u JOIN user_roles ur ON ur.user_id=u.id JOIN roles r ON r.id=ur.role_id WHERE u.id=? AND u.status='ACTIVE' AND r.name='ADMIN')",Boolean.class,actor)))throw new ForbiddenException("Authorized policy reviewer required");}
 private void audit(UUID actor,String action,UUID id){db.update("INSERT INTO audit_logs(actor_user_id,action,entity_type,entity_id,metadata) VALUES (?,?,'GOVERNANCE',?,'{}')",actor,action,id);}
 @Transactional public void review(UUID id,UUID actor,String reference){
  staff(actor);if(reference==null||!reference.matches("[A-Za-z0-9._/-]{3,100}"))throw new IllegalArgumentException("Provide a safe external review reference");
  var rows=db.queryForList("SELECT status,updated_by FROM policy_documents WHERE id=? FOR UPDATE",id);if(rows.isEmpty())throw new NotFoundException("Policy draft was not found");
  var row=rows.getFirst();if(!"DRAFT".equals(row.get("status"))||Objects.equals(actor,row.get("updated_by")))throw new ForbiddenException("A different authorized reviewer must review an unpublished draft");
  db.update("UPDATE policy_documents SET review_status='APPROVED',reviewed_by=?,reviewed_at=CURRENT_TIMESTAMP,review_reference=? WHERE id=?",actor,reference,id);audit(actor,"POLICY_REVIEWED",id);
 }
 @Transactional public void publish(UUID id,UUID actor){
  staff(actor);if(!enabled)throw new ForbiddenException("Legal publication is disabled pending approved rollout");
  var key=db.queryForList("SELECT policy_key FROM policy_documents WHERE id=?",String.class,id);if(key.isEmpty())throw new NotFoundException("Policy draft was not found");
  db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))","policy:"+key.getFirst());
  var row=db.queryForMap("SELECT * FROM policy_documents WHERE id=? FOR UPDATE",id);
  if(!"DRAFT".equals(row.get("status"))||!"APPROVED".equals(row.get("review_status"))||row.get("reviewed_by")==null)throw new ConflictException("Only independently approved drafts can be published");
  if(!"en".equals(row.get("locale"))&&Set.of("terms","listener-code").contains(key.getFirst()))throw new ForbiddenException("Translated binding agreements require reviewed locale-aware acceptance support");
  if(Boolean.TRUE.equals(db.queryForObject("SELECT effective_at>CURRENT_TIMESTAMP FROM policy_documents WHERE id=?",Boolean.class,id)))throw new ConflictException("Policy effective date has not arrived");
  db.update("UPDATE policy_documents SET status='RETIRED' WHERE policy_key=? AND locale=? AND status='PUBLISHED'",key.getFirst(),row.get("locale"));
  db.update("UPDATE policy_documents SET status='PUBLISHED',published_at=CURRENT_TIMESTAMP,effective_at=COALESCE(effective_at,CURRENT_TIMESTAMP) WHERE id=?",id);audit(actor,"POLICY_PUBLISHED",id);
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> rightsQueue(){return db.queryForList("SELECT id,user_id,kind,status,created_at FROM data_rights_requests WHERE status IN ('RECEIVED','IN_REVIEW','NEEDS_INFORMATION') ORDER BY created_at LIMIT 100");}
 @Transactional public void resolveRight(UUID id,UUID actor,String status,String reference){
  staff(actor);if(!Set.of("IN_REVIEW","NEEDS_INFORMATION","RESOLVED","DECLINED").contains(status)||reference==null||!reference.matches("[A-Za-z0-9._/-]{3,100}"))throw new IllegalArgumentException("Invalid request status or safe case reference");
  var rows=db.queryForList("SELECT status FROM data_rights_requests WHERE id=? FOR UPDATE",id);if(rows.isEmpty())throw new NotFoundException("Rights request was not found");
  if(Set.of("RESOLVED","DECLINED").contains(rows.getFirst().get("status")))throw new ConflictException("This request is already closed");
  db.update("UPDATE data_rights_requests SET status=?,resolution_reference=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",status,reference,id);audit(actor,"RIGHTS_"+status,id);
 }
}
