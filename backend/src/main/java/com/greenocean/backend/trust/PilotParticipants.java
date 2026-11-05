package com.greenocean.backend.trust;
import com.greenocean.backend.common.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;

@Service @Transactional
public class PilotParticipants {
    private final JdbcTemplate db;private final PilotReadiness readiness;private final PilotGate gate;private final int cap;
    public PilotParticipants(JdbcTemplate db,PilotReadiness readiness,PilotGate gate,@Value("${greenocean.pilot.max-participants:100}")int cap){this.db=db;this.readiness=readiness;this.gate=gate;this.cap=cap;if(cap<100||cap>500)throw new IllegalArgumentException("Pilot capacity must be explicitly bounded100..500");}
    public UUID invite(UUID who,UUID sid,UUID assessment,UUID user){
        readiness.owner(who,sid);var a=readiness.row(assessment,true);
        if(!"OWNER_APPROVED".equals(readiness.assessment(assessment).get("state")))throw new ConflictException("Only a currently owner-approved service may invite participants");
        if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM users u JOIN account_eligibility e ON e.user_id=u.id WHERE u.id=? AND u.status='ACTIVE' AND u.email_verified AND e.age_result='ADULT' AND e.residence_country=? AND(e.current_country IS NULL OR e.current_country=?))",Boolean.class,user,a.get("country_code"),a.get("country_code"))))throw new ForbiddenException("Current eligible account and region required");
        db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended('pilot-cohort-capacity',0))");
        var previous=db.queryForList("SELECT id FROM pilot_participants WHERE assessment_id=? AND user_id=?",UUID.class,assessment,user);if(!previous.isEmpty())return previous.getFirst();
        boolean counted=Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM pilot_participants WHERE user_id=? AND state IN('INVITED','ACCEPTED') AND expires_at>CURRENT_TIMESTAMP)",Boolean.class,user));
        if(!counted&&db.queryForObject("SELECT count(DISTINCT user_id) FROM pilot_participants WHERE state IN('INVITED','ACCEPTED') AND expires_at>CURRENT_TIMESTAMP",Integer.class)>=cap)throw new ConflictException("The explicitly authorized pilot cohort is full");
        UUID id=db.queryForObject("INSERT INTO pilot_participants(assessment_id,user_id,notice_version,notice_sha256,invited_by,expires_at) VALUES(?,?,?,?,?,CURRENT_TIMESTAMP+INTERVAL '24 hours') RETURNING id",UUID.class,assessment,user,a.get("notice_version"),a.get("notice_sha256"),who);
        event(id,who,"INVITED");return id;
    }
    void event(UUID id,UUID who,String state){db.update("INSERT INTO pilot_participant_events(participant_id,actor_id,state) VALUES(?,?,?)",id,who,state);}
    public void choice(UUID user,UUID id,long revision,boolean accept){
        var rows=db.queryForList("SELECT p.* FROM pilot_participants p WHERE p.id=? AND p.user_id=? FOR UPDATE",id,user);if(rows.isEmpty())throw new NotFoundException("Own pilot invitation unavailable");var p=rows.getFirst();
        if(((Number)p.get("revision")).longValue()!=revision)throw new ConflictException("Refresh the invitation before deciding");
        if(accept){
            if(!"INVITED".equals(p.get("state"))||!PilotReadiness.time(p.get("expires_at")).isAfter(java.time.Instant.now())||!"OWNER_APPROVED".equals(readiness.assessment((UUID)p.get("assessment_id")).get("state")))throw new ConflictException("Current approved invitation and notice are required");
        }
        String state=accept?"ACCEPTED":"OPTED_OUT";
        db.update("UPDATE pilot_participants SET state=?,revision=revision+1 WHERE id=?",state,id);event(id,user,state);
        if(!accept){
            db.update("UPDATE listener_wait_queue SET status='CANCELLED',reason_code='PILOT_OPT_OUT' WHERE member_id=? AND status IN('WAITING','OFFERED')",user);
            db.update("UPDATE live_support_requests SET status='CANCELLED',reason_code='PILOT_OPT_OUT',ended_at=CURRENT_TIMESTAMP WHERE member_id=? AND status IN('REQUESTED','ACCEPTED')",user);
        }
    }
    public void revoke(UUID who,UUID sid,UUID id){
        readiness.owner(who,sid);var rows=db.queryForList("SELECT id FROM pilot_participants WHERE id=? FOR UPDATE",id);if(rows.isEmpty())throw new NotFoundException("Invitation unavailable");
        db.update("UPDATE pilot_participants SET state='REVOKED',revision=revision+1 WHERE id=?",id);event(id,who,"REVOKED");
    }
    public Object mine(UUID who){
        var invites=db.queryForList("SELECT p.id,p.state,p.revision,p.notice_version,p.expires_at,a.service FROM pilot_participants p JOIN pilot_assessments a ON a.id=p.assessment_id WHERE p.user_id=? ORDER BY p.created_at DESC LIMIT 50",who);
        return Map.of("invitations",invites,"notice",readiness.notice(),"runtimePilotConfigured",gate.configured(),"existingRightsRemainAvailable",true,"sensitiveSatisfactionDataCollected",false,"participantCap",cap);
    }
}
