package com.greenocean.backend.trust;

import com.greenocean.backend.common.exception.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

/** Fixed projections. Source evidence remains under its existing authorization. */
@Service
public class TrustSources {
    private final JdbcTemplate db;
    public TrustSources(JdbcTemplate db){this.db=db;}
    public record Source(String kind,UUID id,String domain,String category,String severity,String snapshot,boolean complete,boolean appealed,boolean pendingAppeal,Set<UUID> conflicts){}
    public Source read(String kind,UUID id){
        String table=switch(kind){case "REPORT"->"reports";case "SUPPORT_REQUEST"->"support_requests";case "CLINIC_INCIDENT"->"clinic_operational_incidents";case "REPUTATION_CASE"->"clinic_reputation_cases";default->throw new IllegalArgumentException("Choose an existing source workflow");};
        db.queryForList("SELECT id FROM "+table+" WHERE id=? FOR UPDATE",id);
        return switch(kind){
            case "REPORT" -> report(id);
            case "SUPPORT_REQUEST" -> {
                var rows=db.queryForList("SELECT status,user_id FROM support_requests WHERE id=?",id);if(rows.isEmpty())throw missing();var r=rows.getFirst();
                yield new Source(kind,id,"ACCOUNT","ACCOUNT_ACCESS","MEDIUM",(String)r.get("status"),Set.of("RESOLVED","CLOSED").contains(r.get("status")),false,false,ids(r.get("user_id")));
            }
            case "CLINIC_INCIDENT" -> {
                var rows=db.queryForList("SELECT i.kind,i.reporter_id,c.requester_user_id,c.professional_user_id,COALESCE((SELECT string_agg(r.id::text||':'||COALESCE(d.id::text,'PENDING'),',' ORDER BY r.id) FROM clinic_incident_reviews r LEFT JOIN clinic_incident_decisions d ON d.review_id=r.id WHERE r.incident_id=i.id),'UNREVIEWED') AS snapshot,EXISTS(SELECT 1 FROM clinic_incident_reviews r JOIN clinic_incident_decisions d ON d.review_id=r.id WHERE r.incident_id=i.id AND r.classification<>'UNRESOLVED') AND NOT EXISTS(SELECT 1 FROM clinic_incident_reviews pending LEFT JOIN clinic_incident_decisions pd ON pd.review_id=pending.id WHERE pending.incident_id=i.id AND(pd.id IS NULL OR pending.classification='UNRESOLVED')) AS complete FROM clinic_operational_incidents i JOIN consultation_requests c ON c.id=i.appointment_id WHERE i.id=?",id);if(rows.isEmpty())throw missing();var r=rows.getFirst();
                boolean finance="PAYMENT".equals(r.get("kind"));yield new Source(kind,id,finance?"FINANCE":"CLINIC",finance?"FINANCIAL_SERVICE":"PLATFORM_SAFETY","HIGH",(String)r.get("snapshot"),Boolean.TRUE.equals(r.get("complete")),false,false,ids(r.get("reporter_id"),r.get("requester_user_id"),r.get("professional_user_id")));
            }
            case "REPUTATION_CASE" -> {
                var rows=db.queryForList("SELECT state,kind,requester_id,professional_id,reviewer_id FROM clinic_reputation_cases WHERE id=?",id);if(rows.isEmpty())throw missing();var r=rows.getFirst();boolean appeal="RANKING_APPEAL".equals(r.get("kind"));
                yield new Source(kind,id,"CLINIC","PROFESSIONAL_CONDUCT","HIGH",Objects.toString(r.get("state")),!"OPEN".equals(r.get("state")),appeal,appeal&&"OPEN".equals(r.get("state")),ids(r.get("requester_id"),r.get("professional_id"),r.get("reviewer_id")));
            }
            default -> throw new IllegalArgumentException("Choose an existing source workflow");
        };
    }
    private Source report(UUID id){
        var rows=db.queryForList("SELECT r.status,r.severity,r.reason,r.reporter_id,r.target_user_id,r.post_id,r.comment_id,r.context_group_id,r.community_id,r.event_id,r.conversation_id,p.author_id AS post_author,c.author_id AS comment_author,EXISTS(SELECT 1 FROM live_support_requests l WHERE l.conversation_id=r.conversation_id) AS listener_report,EXISTS(SELECT 1 FROM moderation_appeals a WHERE a.report_id=r.id) AS appealed,EXISTS(SELECT 1 FROM moderation_appeals a WHERE a.report_id=r.id AND a.status IN('OPEN','REVIEWING')) AS pending_appeal,COALESCE((SELECT string_agg(a.id::text||':'||a.status,',' ORDER BY a.id) FROM moderation_appeals a WHERE a.report_id=r.id),'NONE') AS appeals FROM reports r LEFT JOIN posts p ON p.id=r.post_id LEFT JOIN comments c ON c.id=r.comment_id WHERE r.id=?",id);
        if(rows.isEmpty())throw missing();var r=rows.getFirst();
        String domain=r.get("context_group_id")!=null||r.get("community_id")!=null||r.get("event_id")!=null?"GROUP":Boolean.TRUE.equals(r.get("listener_report"))?"LISTENER":r.get("conversation_id")!=null?"ACCOUNT":"SOCIAL";
        String category=domain.equals("GROUP")?"GROUP_MISCONDUCT":domain.equals("LISTENER")?"LISTENER_MISCONDUCT":r.get("reason").equals("HARASSMENT")?"HARASSMENT":r.get("post_id")!=null?"POST":r.get("comment_id")!=null?"COMMENT":"PRIVACY";
        return new Source("REPORT",id,domain,category,(String)r.get("severity"),r.get("status")+":"+r.get("appeals"),Set.of("RESOLVED","DISMISSED").contains(r.get("status")),Boolean.TRUE.equals(r.get("appealed")),Boolean.TRUE.equals(r.get("pending_appeal")),ids(r.get("reporter_id"),r.get("target_user_id"),r.get("post_author"),r.get("comment_author")));
    }
    private Set<UUID> ids(Object... values){Set<UUID> result=new HashSet<>();for(Object value:values)if(value instanceof UUID id)result.add(id);return result;}
    private NotFoundException missing(){return new NotFoundException("Authorized source reference was not found");}
}
