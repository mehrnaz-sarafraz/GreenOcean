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
    public AccessPolicy(JdbcTemplate db, @Value("${greenocean.phase-a.legacy-grace-until:2027-01-01T00:00:00Z}") Instant deadline) {
        this.db = db; this.legacyDeadline = deadline;
    }
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
    public void country(String code) {
        if (code == null || !Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM geography_countries WHERE code=?)", Boolean.class, code)))
            throw new IllegalArgumentException("Select a valid country of residence");
    }
    public void requireDisplayReview(UUID userId) {
        if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM users u JOIN account_eligibility e ON e.user_id=u.id WHERE u.id=? AND u.phase_a_enrolled AND e.display_reviewed_at IS NULL)",Boolean.class,userId)))
            throw new ForbiddenException("Review your public display name before your first contribution");
    }
}
