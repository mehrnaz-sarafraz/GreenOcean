package com.greenocean.backend.trust;
import com.greenocean.backend.common.exception.ForbiddenException;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import java.util.*;

/** Assessment approval never toggles this separately configured runtime gate. */
@Service
public class PilotGate {
    private final JdbcTemplate db;private final PilotReadiness readiness;private final boolean enabled;
    public PilotGate(JdbcTemplate db,PilotReadiness readiness,@Value("${greenocean.pilot.enabled:false}")boolean enabled){this.db=db;this.readiness=readiness;this.enabled=enabled;}
    @EventListener(ApplicationReadyEvent.class)public void enforce(){if(enabled)db.update("UPDATE pilot_rollout_policy SET enforcement_required=TRUE WHERE id=TRUE");}
    public boolean enforced(){return enabled||Boolean.TRUE.equals(db.queryForObject("SELECT enforcement_required FROM pilot_rollout_policy WHERE id=TRUE",Boolean.class));}
    public boolean configured(){return enabled;}
    public boolean eligible(UUID who,String service){
        if(!enforced())return true;if(!enabled)return false;
        var candidates=db.queryForList("SELECT p.id,p.assessment_id,a.country_code FROM pilot_participants p JOIN pilot_assessments a ON a.id=p.assessment_id JOIN users u ON u.id=p.user_id JOIN account_eligibility e ON e.user_id=u.id WHERE p.user_id=? AND a.service=? AND a.source_commit=? AND p.state='ACCEPTED' AND p.expires_at>CURRENT_TIMESTAMP AND u.status='ACTIVE' AND u.email_verified AND u.deleted_at IS NULL AND e.age_result='ADULT' AND e.residence_country=a.country_code AND(e.current_country IS NULL OR e.current_country=a.country_code) ORDER BY p.created_at DESC LIMIT 20",who,service,readiness.source());
        for(var candidate:candidates){UUID id=(UUID)candidate.get("assessment_id");if(id.equals(readiness.latest(service,((String)candidate.get("country_code")).trim()))&&"OWNER_APPROVED".equals(readiness.assessment(id).get("state")))return true;}
        return false;
    }
    public void require(UUID who,String service){
        if(!enforced())return;
        // Keep opt-out/revocation and source admissions ordered inside the caller's transaction.
        db.queryForList("SELECT a.id FROM pilot_assessments a JOIN pilot_participants p ON p.assessment_id=a.id WHERE p.user_id=? AND a.service=? FOR SHARE OF a,p",who,service);
        if(!eligible(who,service))throw new ForbiddenException("This service requires a current authorized pilot invitation,explicit consent and live operating approval");
    }
    public static void require(PilotGate gate,JdbcTemplate db,UUID who,String service){
        if(gate!=null)gate.require(who,service);
        else if(Boolean.TRUE.equals(db.queryForObject("SELECT enforcement_required FROM pilot_rollout_policy WHERE id=TRUE",Boolean.class)))throw new ForbiddenException("Pilot runtime authorization is unavailable");
    }
}
