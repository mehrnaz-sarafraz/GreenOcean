package com.greenocean.backend.identity;

import com.greenocean.backend.common.exception.ForbiddenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;

@Service
public class AccessPolicy {
    private final JdbcTemplate db;
    private final Instant legacyDeadline;
    private final boolean clinicEnabled;
    @org.springframework.beans.factory.annotation.Autowired
    public AccessPolicy(JdbcTemplate db, @Value("${greenocean.phase-a.legacy-grace-until:2027-01-01T00:00:00Z}") Instant deadline,@Value("${greenocean.clinic.enabled:false}")boolean clinicEnabled) {
        this.db = db; this.legacyDeadline = deadline;this.clinicEnabled=clinicEnabled;
    }
    public AccessPolicy(JdbcTemplate db,Instant deadline){this(db,deadline,false);}
    public void require(UUID userId, String service) {
        var rows = db.queryForList("""
          SELECT u.status,u.email_verified,u.phase_a_enrolled,e.age_result,e.residence_country,e.current_country
          FROM users u LEFT JOIN account_eligibility e ON e.user_id=u.id WHERE u.id=?
          """, userId);
        if (rows.isEmpty()) throw new ForbiddenException("Account is unavailable");
        var row = rows.getFirst();
        if (!"ACTIVE".equals(row.get("status"))) throw new ForbiddenException("Activate your account before using social services");
        if("INELIGIBLE".equals(row.get("age_result"))) throw new ForbiddenException("This account is limited to public resources");
        boolean legacyGrace = !Boolean.TRUE.equals(row.get("phase_a_enrolled")) && Instant.now().isBefore(legacyDeadline);
        if (!legacyGrace && (!Boolean.TRUE.equals(row.get("email_verified")) || !"ADULT".equals(row.get("age_result"))))
            throw new ForbiddenException("Verify your email and adult eligibility in account activation");
        // Legacy access is temporary and never grants newly introduced care services.
        if (legacyGrace && "COMMUNITY".equals(service)) return;
        if (!available((String) row.get("residence_country"), service) || (row.get("current_country")!=null && !available((String)row.get("current_country"),service)))
            throw new ForbiddenException("This service is unavailable in your region. Public resources remain available.");
    }
    public boolean available(String country, String service) {
        return country != null && Boolean.TRUE.equals(db.queryForObject("""
          SELECT EXISTS(SELECT 1 FROM regional_services WHERE country_code=? AND service=? AND status='AVAILABLE')
          """, Boolean.class, country, service));
    }
    public void requireCare(UUID requester,UUID professional){requireCare(requester,professional,"TEXT");}
    public void requireCare(UUID requester,UUID professional,String mode) {
        if(!java.util.Set.of("TEXT","AUDIO","VIDEO").contains(mode))throw new ForbiddenException("Unsupported professional consultation modality");
        if(!clinicEnabled||!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM account_eligibility e WHERE e.user_id=? AND professional_scope_live(?,e.residence_country,?) AND(e.current_country IS NULL OR professional_scope_live(?,e.current_country,?)))",Boolean.class,requester,professional,mode,professional,mode)))throw new ForbiddenException("Current independent professional and platform approvals are required");
        for(UUID participant:List.of(requester,professional))if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM users u JOIN account_eligibility e ON e.user_id=u.id WHERE u.id=? AND u.status='ACTIVE' AND u.email_verified AND e.age_result='ADULT')",Boolean.class,participant)))throw new ForbiddenException("New clinical care requires verified adult eligibility for both participants; legacy grace is insufficient");
        require(requester,"PROFESSIONAL_CARE");require(professional,"PROFESSIONAL_CARE");
        if(!Boolean.TRUE.equals(db.queryForObject("""
          SELECT EXISTS(SELECT 1 FROM professional_care_regions scope
          JOIN professional_profiles p ON p.user_id=scope.professional_user_id
          JOIN account_eligibility e ON e.residence_country=scope.country_code
          WHERE e.user_id=? AND scope.professional_user_id=? AND p.verification_status='VERIFIED'
          AND scope.expires_at>CURRENT_TIMESTAMP
          AND (e.current_country IS NULL OR e.current_country=scope.country_code OR EXISTS
            (SELECT 1 FROM professional_care_regions current_scope WHERE current_scope.professional_user_id=scope.professional_user_id
             AND current_scope.country_code=e.current_country AND current_scope.expires_at>CURRENT_TIMESTAMP)))
          """,Boolean.class,requester,professional)))
            throw new ForbiddenException("Professional care is not approved for this professional in your region");
    }
    public void country(String code) {
        if (code == null || !Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM geography_countries WHERE code=?)", Boolean.class, code)))
            throw new IllegalArgumentException("Select a valid country of residence");
    }
    public void requireDisplayReview(UUID userId) {
        if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM users u JOIN account_eligibility e ON e.user_id=u.id WHERE u.id=? AND u.phase_a_enrolled AND e.display_reviewed_at IS NULL)",Boolean.class,userId)))
            throw new ForbiddenException("Review your public display name before your first contribution");
    }
}
