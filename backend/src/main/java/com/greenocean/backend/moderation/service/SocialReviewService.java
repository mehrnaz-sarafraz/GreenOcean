package com.greenocean.backend.moderation.service;

import com.greenocean.backend.common.exception.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** Review current content revisions only. Ordinary responses never include safety attribution. */
@Service
public class SocialReviewService {
    private final JdbcTemplate db;
    public SocialReviewService(JdbcTemplate db) { this.db = db; }

    private void staff(UUID actor) {
        if (!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id JOIN users u ON u.id=ur.user_id WHERE ur.user_id=? AND r.name IN ('ADMIN','MODERATOR') AND u.status='ACTIVE' AND u.deleted_at IS NULL)",Boolean.class,actor)))
            throw new ForbiddenException("Current moderation authorization is required");
    }
    @Transactional(readOnly=true)
    public List<Map<String,Object>> mine(UUID owner) { return mine(owner,0); }
    @Transactional(readOnly=true)
    public List<Map<String,Object>> mine(UUID owner,int page) {
        return db.queryForList("""
            SELECT a.id,a.post_id,a.comment_id,a.revision,a.policy_version,a.outcome,a.reason_code,
                   a.decision_note,a.assessed_at,a.reviewed_at,ap.id AS appeal_id,ap.status AS appeal_status,ap.decision_note AS appeal_note
            FROM social_assessments a LEFT JOIN posts p ON p.id=a.post_id LEFT JOIN comments c ON c.id=a.comment_id
            LEFT JOIN moderation_appeals ap ON ap.assessment_id=a.id
            WHERE (p.author_id=? OR c.author_id=?) ORDER BY a.assessed_at DESC,a.id DESC LIMIT 20 OFFSET ?
            """,owner,owner,Math.min(1000,Math.max(0,page))*20);
    }
    @Transactional
    public List<Map<String,Object>> queue(UUID actor,int page) {
        staff(actor);
        var result=db.queryForList("""
            SELECT a.id,a.post_id,a.comment_id,a.revision,a.policy_version,a.outcome,a.reason_code,a.assessed_at,
                   COALESCE(p.body,c.body) AS body,ap.id AS appeal_id,ap.reason AS appeal_reason
            FROM social_assessments a LEFT JOIN posts p ON p.id=a.post_id LEFT JOIN comments c ON c.id=a.comment_id
            LEFT JOIN moderation_appeals ap ON ap.assessment_id=a.id AND ap.status IN ('OPEN','REVIEWING')
            WHERE a.revision=COALESCE(p.revision,c.revision) AND COALESCE(p.deleted_at,c.deleted_at) IS NULL
              AND COALESCE(p.status,c.status) <> 'DELETED' AND (a.outcome <> 'ALLOW' AND a.reviewed_at IS NULL OR ap.id IS NOT NULL)
            ORDER BY a.assessed_at,a.id LIMIT 20 OFFSET ?
            """,Math.min(1000,Math.max(0,page))*20);
        audit(actor,"SOCIAL_QUEUE_READ",null); return result;
    }
    private Map<String,Object> lock(UUID id) {
        var rows=db.queryForList("SELECT * FROM social_assessments WHERE id=? FOR UPDATE",id);
        if(rows.isEmpty())throw new NotFoundException("Decision was not found");
        return rows.getFirst();
    }
    private Map<String,Object> content(Map<String,Object> assessment) {
        boolean post=assessment.get("post_id")!=null;
        var rows=db.queryForList("SELECT id,author_id,revision,status FROM "+(post?"posts":"comments")+" WHERE id=? FOR UPDATE",assessment.get(post?"post_id":"comment_id"));
        if(rows.isEmpty() || "DELETED".equals(rows.getFirst().get("status")))throw new NotFoundException("Content was not found");
        if(!assessment.get("revision").equals(rows.getFirst().get("revision")))throw new ConflictException("Content changed; review the current revision");
        return rows.getFirst();
    }
    @Transactional
    public UUID appeal(UUID owner,UUID id,String reason) {
        var a=lock(id);var c=content(a);
        if(!owner.equals(c.get("author_id")))throw new NotFoundException("Decision was not found");
        if("ALLOW".equals(a.get("outcome")))throw new ConflictException("This content is already allowed");
        if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM moderation_appeals WHERE assessment_id=?)",Boolean.class,id)))throw new ConflictException("An appeal already exists for this revision");
        db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))","safety-submissions:"+owner);
        if(db.queryForObject("SELECT count(*) FROM moderation_appeals WHERE appellant_user_id=? AND created_at>CURRENT_TIMESTAMP-INTERVAL '1 hour'",Long.class,owner)>=20)
            throw new ConflictException("Please pause before submitting more appeals");
        UUID appeal=UUID.randomUUID();
        db.update("INSERT INTO moderation_appeals(id,appellant_user_id,assessment_id,reason) VALUES(?,?,?,?)",appeal,owner,id,reason.trim());
        return appeal;
    }
    @Transactional
    public void decide(UUID actor,UUID id,long revision,String outcome,String note) {
        staff(actor);
        if(!Set.of("ALLOW","REQUEST_REVISION","BLOCK_PROHIBITED").contains(outcome))throw new IllegalArgumentException("Unsupported moderation decision");
        var a=lock(id);var c=content(a);
        if(revision!=((Number)a.get("revision")).longValue())throw new ConflictException("Content changed; reload before deciding");
        if(actor.equals(c.get("author_id")))throw new ForbiddenException("Another moderator must review your content");
        var appeals=db.queryForList("SELECT id FROM moderation_appeals WHERE assessment_id=? AND status IN ('OPEN','REVIEWING') FOR UPDATE",id);
        if(a.get("reviewed_at")!=null && appeals.isEmpty())throw new ConflictException("Decision already recorded");
        if(!appeals.isEmpty() && actor.equals(a.get("reviewer_id")))throw new ForbiddenException("An independent moderator must decide the appeal");
        boolean post=a.get("post_id")!=null;String table=post?"posts":"comments";
        db.update("UPDATE "+table+" SET status=?,safety_outcome=?,safety_reason='HUMAN_REVIEW' WHERE id=?",outcome.equals("ALLOW")?"PUBLISHED":"HIDDEN",outcome,c.get("id"));
        db.update("INSERT INTO social_review_decisions(assessment_id,revision,outcome,reviewer_id,note) VALUES(?,?,?,?,?)",id,revision,outcome,actor,note.trim());
        db.update("UPDATE social_assessments SET outcome=?,reason_code='HUMAN_REVIEW',decision_note=?,reviewer_id=?,reviewed_at=CURRENT_TIMESTAMP WHERE id=?",outcome,note.trim(),actor,id);
        db.update("UPDATE moderation_appeals SET status=?,decision_note=?,decided_by=?,decided_at=CURRENT_TIMESTAMP WHERE assessment_id=? AND status IN ('OPEN','REVIEWING')",outcome.equals("ALLOW")?"OVERTURNED":"UPHELD",note.trim(),actor,id);
        db.update("INSERT INTO notifications(user_id,type,title,body) SELECT ?,'SYSTEM_NOTICE','Content review updated','Open your content decisions to view the outcome.' WHERE COALESCE((SELECT notify_replies FROM user_preferences WHERE user_id=?),TRUE)",c.get("author_id"),c.get("author_id"));
        audit(actor,"SOCIAL_"+outcome,id);
    }
    @Transactional
    public Map<String,Object> attribution(UUID actor,UUID id,String purpose) {
        staff(actor);
        if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=? AND r.name='ADMIN')",Boolean.class,actor)))throw new ForbiddenException("Administrator authorization is required for attribution");
        var a=lock(id);var c=content(a);
        audit(actor,"SOCIAL_IDENTITY_REVIEW",id);
        db.update("UPDATE audit_logs SET metadata=jsonb_build_object('purpose',?::text) WHERE id=(SELECT id FROM audit_logs WHERE actor_user_id=? AND entity_id=? AND action='SOCIAL_IDENTITY_REVIEW' ORDER BY created_at DESC,id DESC LIMIT 1)",purpose.trim(),actor,id);
        return Map.of("authorId",c.get("author_id"));
    }
    private void audit(UUID actor,String action,UUID id) {
        db.update("INSERT INTO audit_logs(actor_user_id,action,entity_type,entity_id) VALUES(?,?,'SOCIAL_ASSESSMENT',?)",actor,action,id);
    }
}
