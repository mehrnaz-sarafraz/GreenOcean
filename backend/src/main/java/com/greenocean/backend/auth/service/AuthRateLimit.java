package com.greenocean.backend.auth.service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.Duration;

@Service
public class AuthRateLimit {
    private final JdbcTemplate db;
    private final TokenService tokens;
    public AuthRateLimit(JdbcTemplate db,TokenService tokens) {this.db=db;this.tokens=tokens;}
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public boolean allow(String key,int maximum,Duration window) {
        String hash=tokens.hashRefreshToken(key);
        Integer attempts=db.queryForObject("""
          INSERT INTO auth_rate_windows(key_hash,attempts,expires_at) VALUES (?,1,CURRENT_TIMESTAMP + (? * INTERVAL '1 second'))
          ON CONFLICT(key_hash) DO UPDATE SET
          attempts=CASE WHEN auth_rate_windows.expires_at <= CURRENT_TIMESTAMP THEN 1 ELSE auth_rate_windows.attempts+1 END,
          expires_at=CASE WHEN auth_rate_windows.expires_at <= CURRENT_TIMESTAMP THEN EXCLUDED.expires_at ELSE auth_rate_windows.expires_at END
          RETURNING attempts
          """,Integer.class,hash,window.toSeconds());
        return attempts<=maximum;
    }
}
