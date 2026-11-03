package com.greenocean.backend.trust;

import com.greenocean.backend.auth.service.RecentAuthentication;
import com.greenocean.backend.common.exception.*;
import com.greenocean.backend.identity.AccessPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

/** Coordination never grants source-domain narrative or attribution permission. */
@Service
public class TrustAuthority {
    public static final Set<String> DOMAINS=Set.of("SOCIAL","GROUP","LISTENER","CLINIC","FINANCE","ACCOUNT","EDUCATION","PLATFORM");
    private final JdbcTemplate db;
    private final RecentAuthentication recent;
    private final AccessPolicy access;
    private final boolean enabled;
    public TrustAuthority(JdbcTemplate db,RecentAuthentication recent,AccessPolicy access,
            @Value("${greenocean.trust.enabled:false}")boolean enabled){this.db=db;this.recent=recent;this.access=access;this.enabled=enabled;}
    public boolean enabled(){return enabled;}
    public static String role(String domain){if(!DOMAINS.contains(domain))throw new IllegalArgumentException("Choose a service category");return "TRUST_"+(domain.equals("GROUP")?"SOCIAL":domain);}
    public boolean has(UUID actor,String role){return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM users u JOIN user_roles ur ON ur.user_id=u.id JOIN roles r ON r.id=ur.role_id WHERE u.id=? AND u.status='ACTIVE' AND u.email_verified AND u.deleted_at IS NULL AND r.name=?)",Boolean.class,actor,role));}
    public void require(UUID actor,UUID session,String domain){
        if(!enabled)throw new ForbiddenException("Trust operations await approved processing and real operations");
        db.queryForList("SELECT id FROM users WHERE id=? FOR SHARE",actor);
        access.require(actor,"COMMUNITY");recent.require(actor,session,true);
        if(!has(actor,role(domain)))throw new ForbiddenException("Current explicit service-purpose authority is required");
    }
    public void budget(UUID actor,String kind,int limit){
        db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))","trust-budget:"+actor+":"+kind);
        if(db.queryForObject("SELECT count(*) FROM audit_logs WHERE actor_user_id=? AND action=? AND created_at>CURRENT_TIMESTAMP-INTERVAL '1 hour'",Integer.class,actor,kind)>=limit)throw new TooManyRequestsException("Please pause before more operational requests",3600);
        audit(actor,null,kind);
    }
    public void audit(UUID actor,UUID id,String action){db.update("INSERT INTO audit_logs(actor_user_id,action,entity_type,entity_id) VALUES(?,?,'TRUST_CASE',?)",actor,action,id);}
}
