package com.greenocean.backend.trust;

import com.greenocean.backend.common.exception.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service @Transactional
public class TrustCoverage {
    private final JdbcTemplate db;private final TrustAuthority authority;private final long recentSeconds;
    public TrustCoverage(JdbcTemplate db,TrustAuthority authority,com.greenocean.backend.auth.service.SessionPolicy policy){this.db=db;this.authority=authority;this.recentSeconds=policy.recent().getSeconds();}
    private static final String LIVE="""
        FROM trust_coverage w JOIN users u ON u.id=w.operator_id JOIN user_sessions s ON s.id=w.session_id
        WHERE w.domain=? AND w.revoked_at IS NULL AND w.starts_at<=CURRENT_TIMESTAMP AND w.ends_at>CURRENT_TIMESTAMP
          AND w.acknowledged_at IS NOT NULL AND w.heartbeat_at>CURRENT_TIMESTAMP-INTERVAL '90 seconds'
          AND u.status='ACTIVE' AND u.email_verified AND u.deleted_at IS NULL
          AND s.user_id=u.id AND s.token_version=w.token_version AND s.session_scope<>'PENDING' AND s.revoked_at IS NULL AND s.expires_at>CURRENT_TIMESTAMP AND s.overall_expires_at>CURRENT_TIMESTAMP AND s.idle_expires_at>CURRENT_TIMESTAMP
          AND s.mfa_authenticated_at>CURRENT_TIMESTAMP-(? * INTERVAL '1 second')
          AND EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=u.id AND r.name=?)
          AND EXISTS(SELECT 1 FROM users a JOIN user_roles ur ON ur.user_id=a.id JOIN roles r ON r.id=ur.role_id WHERE a.id=w.approved_by AND a.status='ACTIVE' AND a.email_verified AND a.deleted_at IS NULL AND r.name='TRUST_PLATFORM' AND EXISTS(SELECT 1 FROM user_roles pr JOIN roles rr ON rr.id=pr.role_id WHERE pr.user_id=a.id AND rr.name='TRUST_'||CASE w.domain WHEN 'GROUP' THEN 'SOCIAL' ELSE w.domain END))
        """;
    public boolean live(String domain,UUID actor){return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 "+LIVE+" AND w.operator_id=?)",Boolean.class,domain,recentSeconds,TrustAuthority.role(domain),actor));}
    public boolean canAssign(String domain,UUID actor){return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 "+LIVE+" AND w.operator_id=? AND (SELECT count(*) FROM trust_cases c WHERE c.assignee_id=w.operator_id AND c.state IN('UNDER_REVIEW','ACTION_PENDING_APPROVAL','APPEAL_REVIEW'))<w.capacity)",Boolean.class,domain,recentSeconds,TrustAuthority.role(domain),actor));}
    @Transactional(readOnly=true) public Map<String,Object> availability(String domain){return Map.of("domain",domain,"available",authority.enabled()&&Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 "+LIVE+" AND (SELECT count(*) FROM trust_cases c WHERE c.assignee_id=w.operator_id AND c.state IN('UNDER_REVIEW','ACTION_PENDING_APPROVAL','APPEAL_REVIEW'))<w.capacity)",Boolean.class,domain,recentSeconds,TrustAuthority.role(domain))),"emergencyResponse",false,"continuousMonitoring",false);}
    public UUID schedule(UUID who,UUID sid,String domain,UUID operator,Instant start,Instant end,int capacity){authority.require(who,sid,domain);if(!authority.has(who,"TRUST_PLATFORM")||who.equals(operator))throw new ForbiddenException("Independent explicit staffing authority is required");if(!authority.has(operator,TrustAuthority.role(domain)))throw new ConflictException("Operator needs current service-purpose authorization");if(start==null||end==null||!end.isAfter(Instant.now())||!end.isAfter(start)||end.isAfter(start.plusSeconds(43200))||start.isAfter(Instant.now().plusSeconds(30*86400))||capacity<1||capacity>50)throw new IllegalArgumentException("Choose a bounded future shift up to twelve hours and capacity 1–50");authority.budget(who,"TRUST_COVERAGE_WRITE",30);UUID id=UUID.randomUUID();db.update("INSERT INTO trust_coverage(id,domain,operator_id,approved_by,starts_at,ends_at,capacity) VALUES(?,?,?,?,?,?,?)",id,domain,operator,who,start.atOffset(ZoneOffset.UTC),end.atOffset(ZoneOffset.UTC),capacity);return id;}
    public void acknowledge(UUID who,UUID sid,UUID id){var rows=db.queryForList("SELECT domain FROM trust_coverage WHERE id=? AND operator_id=? AND revoked_at IS NULL AND starts_at<=CURRENT_TIMESTAMP AND ends_at>CURRENT_TIMESTAMP FOR UPDATE",id,who);if(rows.isEmpty())throw new NotFoundException("Current assigned shift required");authority.require(who,sid,(String)rows.getFirst().get("domain"));db.update("UPDATE trust_coverage SET acknowledged_at=COALESCE(acknowledged_at,CURRENT_TIMESTAMP),heartbeat_at=CURRENT_TIMESTAMP,session_id=?,token_version=(SELECT token_version FROM user_sessions WHERE id=?) WHERE id=?",sid,sid,id);authority.audit(who,id,"TRUST_SHIFT_ACKNOWLEDGED");}
    public void revoke(UUID who,UUID sid,UUID id){var rows=db.queryForList("SELECT domain,operator_id FROM trust_coverage WHERE id=? FOR UPDATE",id);if(rows.isEmpty())throw new NotFoundException("Shift unavailable");var w=rows.getFirst();authority.require(who,sid,(String)w.get("domain"));if(!who.equals(w.get("operator_id"))&&!authority.has(who,"TRUST_PLATFORM"))throw new ForbiddenException("Staffing authority required");db.update("UPDATE trust_coverage SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE id=?",id);authority.audit(who,id,"TRUST_SHIFT_REVOKED");}
    public List<Map<String,Object>> shifts(UUID who,UUID sid,String domain){authority.require(who,sid,domain);return db.queryForList("SELECT id,operator_id,starts_at,ends_at,capacity,acknowledged_at,heartbeat_at,revoked_at FROM trust_coverage WHERE domain=? AND ends_at>CURRENT_TIMESTAMP AND(operator_id=? OR ?) ORDER BY starts_at,id LIMIT 50",domain,who,authority.has(who,"TRUST_PLATFORM"));}
}
