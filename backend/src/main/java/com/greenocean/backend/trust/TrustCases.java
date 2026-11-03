package com.greenocean.backend.trust;

import com.greenocean.backend.common.exception.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @Transactional
public class TrustCases {
    private final JdbcTemplate db;private final TrustAuthority auth;private final TrustSources sources;private final TrustCoverage coverage;
    public TrustCases(JdbcTemplate db,TrustAuthority auth,TrustSources sources,TrustCoverage coverage){this.db=db;this.auth=auth;this.sources=sources;this.coverage=coverage;}
    private static final String SAFE="id,source_kind,source_id,domain,category,severity,state,revision,assignee_id,purpose,result,approval_expires_at,created_at,updated_at";
    public UUID coordinate(UUID who,UUID sid,String kind,UUID sourceId,String domain){
        auth.require(who,sid,domain);TrustSources.Source source=sources.read(kind,sourceId);
        boolean classifiedSupport=kind.equals("SUPPORT_REQUEST")&&Set.of("ACCOUNT","EDUCATION","PLATFORM").contains(domain);
        if(!source.domain().equals(domain)&&!classifiedSupport)throw new NotFoundException("Source unavailable in this purpose queue");
        if(source.conflicts().contains(who))throw new ForbiddenException("Independent coordinator required");
        auth.budget(who,"TRUST_CASE_IMPORT",60);
        String category=classifiedSupport?switch(domain){case "EDUCATION"->"EDUCATIONAL_CONTENT";case "PLATFORM"->"PLATFORM_SAFETY";default->source.category();}:source.category();
        UUID id=UUID.randomUUID();int inserted=db.update("INSERT INTO trust_cases(id,source_kind,source_id,domain,category,severity,purpose,created_by) VALUES(?,?,?,?,?,?,'SAFETY_COORDINATION',?) ON CONFLICT(source_kind,source_id) DO NOTHING",id,kind,sourceId,domain,category,source.severity(),who);
        if(inserted==0){var rows=db.queryForList("SELECT id FROM trust_cases WHERE source_kind=? AND source_id=? AND domain=?",UUID.class,kind,sourceId,domain);if(rows.isEmpty())throw new ConflictException("Source already has purpose-bound coordination");return rows.getFirst();}
        event(id,1,who,null,"RECEIVED","SOURCE_RECEIVED");return id;
    }
    public List<Map<String,Object>> queue(UUID who,UUID sid,String domain,int page,boolean assigned){auth.require(who,sid,domain);if(page<0||page>10000)throw new IllegalArgumentException("Invalid queue page");return db.queryForList("SELECT "+SAFE+" FROM trust_cases WHERE domain=? AND (?=FALSE OR assignee_id=?) ORDER BY CASE severity WHEN 'CRITICAL' THEN 0 WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END,created_at,id LIMIT 20 OFFSET ?",domain,assigned,who,page*20);}
    public Map<String,Object> detail(UUID who,UUID sid,UUID id,String domain){auth.require(who,sid,domain);var rows=db.queryForList("SELECT "+SAFE+" FROM trust_cases WHERE id=? AND domain=?",id,domain);if(rows.isEmpty())throw new NotFoundException("Case unavailable");auth.audit(who,id,"TRUST_CASE_VIEW");return Map.of("case",rows.getFirst(),"history",db.queryForList("SELECT revision,from_state,to_state,reason_code,created_at FROM trust_case_events WHERE case_id=? ORDER BY revision DESC LIMIT 100",id));}
    public void move(UUID who,UUID sid,UUID id,String domain,long revision,String target){
        auth.require(who,sid,domain);var rows=db.queryForList("SELECT * FROM trust_cases WHERE id=? AND domain=? FOR UPDATE",id,domain);if(rows.isEmpty())throw new NotFoundException("Case unavailable");var c=rows.getFirst();
        if(((Number)c.get("revision")).longValue()!=revision)throw new ConflictException("Case changed; reload before deciding");
        String from=(String)c.get("state");TrustSources.Source source=sources.read((String)c.get("source_kind"),(UUID)c.get("source_id"));
        if(source.conflicts().contains(who))throw new ForbiddenException("Independent source reviewer required");
        auth.budget(who,"TRUST_CASE_TRANSITION",120);
        String reason=switch(target){
            case "TRIAGED" -> {require(from,"RECEIVED","WAITING_FOR_OPERATOR","ACTION_PENDING_APPROVAL");yield from.equals("ACTION_PENDING_APPROVAL")?"APPROVAL_REVOKED":"TRIAGE";}
            case "WAITING_FOR_OPERATOR" -> {require(from,"RECEIVED","TRIAGED","UNDER_REVIEW");yield "COVERAGE_UNAVAILABLE";}
            case "UNDER_REVIEW" -> {require(from,"TRIAGED","WAITING_FOR_OPERATOR");present(who,domain);db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))","trust-assignment:"+who);if(!coverage.canAssign(domain,who))throw new ConflictException("No current review capacity");db.update("UPDATE trust_cases SET assignee_id=? WHERE id=?",who,id);yield "REVIEW_STARTED";}
            case "ACTION_PENDING_APPROVAL" -> {require(from,"UNDER_REVIEW","APPEAL_REVIEW");assigned(c,who);present(who,domain);db.update("UPDATE trust_cases SET action_author=?,action_session=?,source_snapshot=?,approval_expires_at=CURRENT_TIMESTAMP+INTERVAL '15 minutes',independent_reviewer=NULL WHERE id=?",who,sid,source.snapshot(),id);yield "DUAL_REVIEW_REQUESTED";}
            case "ACTIONED" -> {require(from,"ACTION_PENDING_APPROVAL");present(who,domain);if(who.equals(c.get("action_author"))||who.equals(c.get("assignee_id")))throw new ForbiddenException("A second independent reviewer is required");if(!Boolean.TRUE.equals(db.queryForObject("SELECT approval_expires_at>CURRENT_TIMESTAMP FROM trust_cases WHERE id=?",Boolean.class,id))||!Objects.equals(c.get("source_snapshot"),source.snapshot()))throw new ConflictException("Approval expired or source evidence changed");auth.require((UUID)c.get("action_author"),(UUID)c.get("action_session"),domain);if(!source.complete()||source.pendingAppeal())throw new ConflictException("Complete the authorized source workflow and pending appeals first");db.update("UPDATE trust_cases SET independent_reviewer=?,result='SOURCE_OUTCOME_CONFIRMED' WHERE id=?",who,id);yield "INDEPENDENT_APPROVAL";}
            case "APPEALED" -> {require(from,"ACTIONED","RESOLVED","CLOSED");if(!source.appealed())throw new ConflictException("Use the existing source appeal route first");yield "SOURCE_APPEALED";}
            case "APPEAL_REVIEW" -> {require(from,"APPEALED");present(who,domain);if(who.equals(c.get("action_author"))||who.equals(c.get("independent_reviewer")))throw new ForbiddenException("Original decision makers cannot review this appeal");db.update("UPDATE trust_cases SET assignee_id=? WHERE id=?",who,id);yield "INDEPENDENT_APPEAL_REVIEW";}
            case "RESOLVED" -> {require(from,"ACTIONED");if(!source.complete()||source.pendingAppeal()||!Objects.equals(source.snapshot(),c.get("source_snapshot")))throw new ConflictException("Current source outcome and finished appeal required");yield "SOURCE_RESOLVED";}
            case "CLOSED" -> {require(from,"RESOLVED");if(!source.complete()||source.pendingAppeal()||!Objects.equals(source.snapshot(),c.get("source_snapshot")))throw new ConflictException("Source changed; independent review is still required");yield "CLOSED";}
            default -> throw new IllegalArgumentException("Unsupported case transition");
        };
        db.update("UPDATE trust_cases SET state=?,revision=revision+1,updated_at=CURRENT_TIMESTAMP WHERE id=?",target,id);
        if(target.equals("TRIAGED"))db.update("UPDATE trust_cases SET action_author=NULL,source_snapshot=NULL,approval_expires_at=NULL,independent_reviewer=NULL WHERE id=?",id);
        event(id,revision+1,who,from,target,reason);
    }
    private void present(UUID who,String domain){if(!coverage.live(domain,who))throw new ConflictException("An acknowledged current staffed shift and recent presence are required");}
    private void assigned(Map<String,Object> c,UUID who){if(!who.equals(c.get("assignee_id")))throw new ForbiddenException("Only the currently assigned independent operator may propose this action");}
    private void require(String actual,String... valid){if(!Set.of(valid).contains(actual))throw new ConflictException("Transition is not available from the current case state");}
    private void event(UUID id,long revision,UUID who,String from,String to,String reason){db.update("INSERT INTO trust_case_events(case_id,revision,actor_id,from_state,to_state,reason_code) VALUES(?,?,?,?,?,?)",id,revision,who,from,to,reason);auth.audit(who,id,"TRUST_CASE_"+to);}
    public List<Map<String,Object>> candidates(UUID who,UUID sid,String domain){auth.require(who,sid,domain);return switch(domain){
        case "SOCIAL","GROUP","LISTENER" -> db.queryForList("SELECT r.id,'REPORT' AS source_kind,r.severity,r.status FROM reports r WHERE NOT EXISTS(SELECT 1 FROM trust_cases c WHERE c.source_kind='REPORT' AND c.source_id=r.id) AND CASE WHEN r.context_group_id IS NOT NULL OR r.community_id IS NOT NULL OR r.event_id IS NOT NULL THEN 'GROUP' WHEN EXISTS(SELECT 1 FROM live_support_requests l WHERE l.conversation_id=r.conversation_id) THEN 'LISTENER' WHEN r.conversation_id IS NOT NULL THEN 'ACCOUNT' ELSE 'SOCIAL' END=? ORDER BY r.created_at,r.id LIMIT 20",domain);
        case "CLINIC" -> db.queryForList("SELECT id,source_kind,status FROM (SELECT id,'REPUTATION_CASE' AS source_kind,state AS status,created_at FROM clinic_reputation_cases UNION ALL SELECT id,'CLINIC_INCIDENT','RECEIVED',created_at FROM clinic_operational_incidents WHERE kind<>'PAYMENT') r WHERE NOT EXISTS(SELECT 1 FROM trust_cases c WHERE c.source_kind=r.source_kind AND c.source_id=r.id) ORDER BY created_at,id LIMIT 20");
        case "FINANCE" -> db.queryForList("SELECT id,'CLINIC_INCIDENT' AS source_kind,'RECEIVED' AS status FROM clinic_operational_incidents r WHERE kind='PAYMENT' AND NOT EXISTS(SELECT 1 FROM trust_cases c WHERE c.source_kind='CLINIC_INCIDENT' AND c.source_id=r.id) ORDER BY created_at,id LIMIT 20");
        default -> db.queryForList("SELECT id,'SUPPORT_REQUEST' AS source_kind,status FROM support_requests r WHERE NOT EXISTS(SELECT 1 FROM trust_cases c WHERE c.source_kind='SUPPORT_REQUEST' AND c.source_id=r.id) ORDER BY created_at,id LIMIT 20");
    };}
}
