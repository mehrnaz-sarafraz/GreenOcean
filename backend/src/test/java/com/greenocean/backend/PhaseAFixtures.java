package com.greenocean.backend;
import org.springframework.jdbc.core.JdbcTemplate;

/** Synthetic adult fixtures for pre-existing content tests. Auth/verification behavior has separate API coverage. */
final class PhaseAFixtures {
    static void activate(JdbcTemplate db,String email) {
        db.update("UPDATE users SET status='ACTIVE',email_verified=TRUE WHERE email=?",email);
        db.update("UPDATE account_eligibility SET display_reviewed_at=CURRENT_TIMESTAMP WHERE user_id=(SELECT id FROM users WHERE email=?)",email);
        db.update("INSERT INTO regional_services(country_code,service,status) VALUES ('IR','COMMUNITY','AVAILABLE') ON CONFLICT(country_code,service) DO UPDATE SET status='AVAILABLE'");
    }
}
