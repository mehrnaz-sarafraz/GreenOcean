package com.greenocean.backend.community.service;

import com.greenocean.backend.common.exception.*;
import com.greenocean.backend.moderation.dto.CreateReportRequest;
import com.greenocean.backend.moderation.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** Group actions narrow access; only independent platform review can reinstate appealed content. */
@Service
public class GroupSafety {
 private final GroupPolicy p;private final ReportService reports;private final GroupActivities activities;
 public GroupSafety(GroupPolicy p,ReportService reports,GroupActivities activities){this.p=p;this.reports=reports;this.activities=activities;}
 @Transactional public UUID report(UUID group,UUID actor,CreateReportRequest in){p.lock(group);p.member(group,actor);
  boolean valid=switch(in.targetType()){
   case "COMMUNITY"->group.equals(in.targetId());
   case "POST"->Boolean.TRUE.equals(p.db.queryForObject("SELECT EXISTS(SELECT 1 FROM posts WHERE id=? AND community_id=? AND can_read_social_post(id,?))",Boolean.class,in.targetId(),group,actor));
   case "COMMENT"->Boolean.TRUE.equals(p.db.queryForObject("SELECT EXISTS(SELECT 1 FROM comments c JOIN posts post ON post.id=c.post_id WHERE c.id=? AND post.community_id=? AND can_read_social_comment(c.id,?))",Boolean.class,in.targetId(),group,actor));
   case "EVENT"->Boolean.TRUE.equals(p.db.queryForObject("SELECT EXISTS(SELECT 1 FROM community_events WHERE id=? AND community_id=?)",Boolean.class,in.targetId(),group));
   case "USER"->Boolean.TRUE.equals(p.db.queryForObject("SELECT active_group_member(?,?)",Boolean.class,group,in.targetId()));
   default->false;};
  if(!valid)GroupPolicy.missing();UUID id=reports.create(actor,in).id();p.db.update("UPDATE reports SET context_group_id=? WHERE id=?",group,id);p.audit(actor,group,"GROUP_REPORT_SUBMITTED");return id;
 }
 @Transactional public List<Map<String,Object>> queue(UUID group,UUID actor){p.manager(group,actor);p.audit(actor,group,"GROUP_REPORT_QUEUE");
  // User/owner/moderator misconduct and anonymous report text go only to platform staff.
  return p.db.queryForList("SELECT r.id,r.post_id,r.comment_id,r.event_id,r.reason,r.status,COALESCE(post.revision,c.revision,e.revision) AS revision,COALESCE(post.body,c.body,e.title) AS body FROM reports r LEFT JOIN posts post ON post.id=r.post_id LEFT JOIN comments c ON c.id=r.comment_id LEFT JOIN community_events e ON e.id=r.event_id WHERE r.context_group_id=? AND r.target_user_id IS NULL AND r.community_id IS NULL AND r.reporter_id<>? AND r.status IN('PENDING','IN_REVIEW') AND (r.post_id IS NULL OR can_read_social_post(r.post_id,?)) AND (r.comment_id IS NULL OR can_read_social_comment(r.comment_id,?)) ORDER BY r.created_at,r.id LIMIT 20",group,actor,actor,actor);
 }
 @Transactional public void hide(UUID group,UUID report,UUID actor,long revision,String reason){p.lock(group);p.manager(group,actor);p.writable(group);p.bound(actor,"GROUP_CONTENT_ACTION",30);
  var rows=p.db.queryForList("SELECT * FROM reports WHERE id=? AND context_group_id=? AND status IN('PENDING','IN_REVIEW') FOR UPDATE",report,group);if(rows.isEmpty())GroupPolicy.missing();var r=rows.getFirst();
  if(actor.equals(r.get("reporter_id"))||r.get("target_user_id")!=null||r.get("community_id")!=null)throw new ForbiddenException("Independent platform review is required");
  boolean post=r.get("post_id")!=null;boolean comment=r.get("comment_id")!=null;
  if(!post&&!comment){throw new ConflictException("Escalate this report to platform review");}
  String table=post?"posts":"comments",column=post?"post_id":"comment_id";UUID id=(UUID)r.get(column);
  var evidence=p.db.queryForList("SELECT id,revision,author_id FROM "+table+" WHERE id=? AND "+(post?"can_read_social_post":"can_read_social_comment")+"(id,?) FOR UPDATE",id,actor);
  if(evidence.isEmpty())GroupPolicy.missing();var item=evidence.getFirst();if(((Number)item.get("revision")).longValue()!=revision)throw new ConflictException("Content changed; review the current revision");
  if(actor.equals(item.get("author_id")))throw new ForbiddenException("Independent platform review is required");
  p.db.update("INSERT INTO community_content_actions(community_id,"+column+",revision,actor_id,reason) VALUES(?,?,?,?,?)",group,id,revision,actor,reason.trim());
  p.db.update("UPDATE reports SET status='RESOLVED',assigned_to=?,resolved_at=CURRENT_TIMESTAMP,resolution_note=? WHERE id=?",actor,reason.trim(),report);
  p.notify((UUID)item.get("author_id"));p.audit(actor,group,"GROUP_CONTENT_HIDDEN");
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> actions(UUID actor){return p.db.queryForList("SELECT a.id,a.community_id,a.post_id,a.comment_id,a.revision,a.reason,a.status,ap.id AS appeal_id,ap.status AS appeal_status,ap.decision_note FROM community_content_actions a LEFT JOIN posts post ON post.id=a.post_id LEFT JOIN comments c ON c.id=a.comment_id LEFT JOIN moderation_appeals ap ON ap.group_action_id=a.id WHERE post.author_id=? OR c.author_id=? ORDER BY a.created_at DESC,a.id DESC LIMIT 30",actor,actor);}
 @Transactional public UUID appeal(UUID actor,UUID action,String reason){var rows=p.db.queryForList("SELECT a.id FROM community_content_actions a LEFT JOIN posts post ON post.id=a.post_id LEFT JOIN comments c ON c.id=a.comment_id WHERE a.id=? AND a.status='HIDDEN' AND (post.author_id=? OR c.author_id=?) FOR UPDATE OF a",action,actor,actor);if(rows.isEmpty())GroupPolicy.missing();p.bound(actor,"GROUP_APPEAL",10);if(Boolean.TRUE.equals(p.db.queryForObject("SELECT EXISTS(SELECT 1 FROM moderation_appeals WHERE group_action_id=?)",Boolean.class,action)))throw new ConflictException("An appeal already exists");UUID id=UUID.randomUUID();p.db.update("INSERT INTO moderation_appeals(id,appellant_user_id,group_action_id,reason) VALUES(?,?,?,?)",id,actor,action,reason.trim());return id;}
 @Transactional public List<Map<String,Object>> staffQueue(UUID actor,String purpose){p.staff(actor);auditPurpose(actor,null,"GROUP_SAFETY_EVIDENCE",purpose);return p.db.queryForList("SELECT r.id,r.context_group_id,r.post_id,r.comment_id,r.event_id,r.reason,r.description,r.status FROM reports r WHERE r.context_group_id IS NOT NULL AND r.status IN('PENDING','IN_REVIEW') ORDER BY r.created_at,r.id LIMIT 30");}
 @Transactional public List<Map<String,Object>> appeals(UUID actor,String purpose){p.staff(actor);auditPurpose(actor,null,"GROUP_APPEALS_EVIDENCE",purpose);return p.db.queryForList("SELECT ap.id,ap.group_action_id,ap.reason,ap.status,a.revision,a.reason AS action_reason,COALESCE(post.body,c.body) AS body FROM moderation_appeals ap JOIN community_content_actions a ON a.id=ap.group_action_id LEFT JOIN posts post ON post.id=a.post_id LEFT JOIN comments c ON c.id=a.comment_id WHERE ap.status IN('OPEN','REVIEWING') ORDER BY ap.created_at,ap.id LIMIT 30");}
 @Transactional public void resolveReport(UUID actor,UUID id,String outcome,String note){p.staff(actor);var rows=p.db.queryForList("SELECT * FROM reports WHERE id=? AND context_group_id IS NOT NULL FOR UPDATE",id);if(rows.isEmpty())GroupPolicy.missing();var r=rows.getFirst();if(actor.equals(r.get("reporter_id"))||actor.equals(r.get("target_user_id")))throw new ForbiddenException("Independent platform review is required");if(!Set.of("PENDING","IN_REVIEW").contains(r.get("status"))||!Set.of("IN_REVIEW","RESOLVED","DISMISSED").contains(outcome))throw new ConflictException("Report transition is unavailable");p.db.update("UPDATE reports SET status=?,assigned_to=?,resolution_note=?,resolved_at=CASE WHEN ?='IN_REVIEW' THEN NULL ELSE CURRENT_TIMESTAMP END WHERE id=?",outcome,actor,note.trim(),outcome,id);p.notify((UUID)r.get("reporter_id"));p.audit(actor,(UUID)r.get("context_group_id"),"GROUP_REPORT_"+outcome);}
 @Transactional public void decideAppeal(UUID actor,UUID id,boolean restore,String note){p.staff(actor);var rows=p.db.queryForList("SELECT a.*,ap.appellant_user_id,ap.status AS appeal_status FROM moderation_appeals ap JOIN community_content_actions a ON a.id=ap.group_action_id WHERE ap.id=? FOR UPDATE OF ap,a",id);if(rows.isEmpty())GroupPolicy.missing();var a=rows.getFirst();if(actor.equals(a.get("actor_id"))||actor.equals(a.get("appellant_user_id")))throw new ForbiddenException("Independent platform review is required");if(!Set.of("OPEN","REVIEWING").contains(a.get("appeal_status")))throw new ConflictException("Appeal already decided");
  // Restoring this group restriction never changes global content status, body, identity or audience.
  if(restore)p.db.update("UPDATE community_content_actions SET status='RESTORED' WHERE id=?",a.get("id"));
  p.db.update("UPDATE moderation_appeals SET status=?,decision_note=?,decided_by=?,decided_at=CURRENT_TIMESTAMP WHERE id=?",restore?"OVERTURNED":"UPHELD",note.trim(),actor,id);p.notify((UUID)a.get("appellant_user_id"));p.audit(actor,(UUID)a.get("community_id"),"GROUP_APPEAL_DECIDED");
 }
 @Transactional public void lifecycle(UUID group,UUID actor,String next,String reason,boolean staff){var g=p.lock(group);String old=(String)g.get("status");if(staff)p.staff(actor);else{p.owner(group,actor);if(!"ARCHIVED".equals(next))throw new ForbiddenException("Owner closure only; platform review is required for restoration");}
  if(!Set.of("ACTIVE","RESTRICTED","SUSPENDED","ARCHIVED").contains(next)||old.equals(next))throw new ConflictException("Group transition is unavailable");
  if("ACTIVE".equals(next)){p.writableOwner(group);}
  p.db.update("UPDATE communities SET status=? WHERE id=?",next,group);p.db.update("INSERT INTO community_lifecycle_decisions(community_id,actor_id,previous_status,next_status,reason) VALUES(?,?,?,?,?)",group,actor,old,next,reason.trim());activities.notifyMembers(group);p.audit(actor,group,"GROUP_STATUS_"+next);
 }
 private void auditPurpose(UUID actor,UUID group,String action,String purpose){if(!Set.of("SAFETY_REVIEW","APPEAL_REVIEW").contains(purpose))throw new IllegalArgumentException("Select an authorized review purpose");p.db.update("INSERT INTO audit_logs(actor_user_id,action,entity_type,entity_id,metadata) VALUES(?,?,'COMMUNITY',?,jsonb_build_object('purpose',?::text))",actor,action,group,purpose);}
}
