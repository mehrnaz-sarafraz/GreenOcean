package com.greenocean.backend.assistance.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AssistanceRepository {
    private final JdbcTemplate jdbcTemplate;

    public AssistanceRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public void invalidatePasswordResets(UUID userId) {
        jdbcTemplate.update("UPDATE password_reset_tokens SET used_at = CURRENT_TIMESTAMP WHERE user_id = ? AND used_at IS NULL", userId);
    }

    public void createPasswordReset(UUID id, UUID userId, String hash, OffsetDateTime expiresAt) {
        jdbcTemplate.update("INSERT INTO password_reset_tokens (id, user_id, token_hash, expires_at) VALUES (?, ?, ?, ?)", id, userId, hash, expiresAt);
    }

    public Optional<UUID> consumePasswordReset(String hash) {
        List<UUID> result = jdbcTemplate.query("""
                UPDATE password_reset_tokens SET used_at = CURRENT_TIMESTAMP
                 WHERE token_hash = ? AND used_at IS NULL AND expires_at > CURRENT_TIMESTAMP
                 RETURNING user_id
                """, (rs, row) -> rs.getObject("user_id", UUID.class), hash);
        return result.stream().findFirst();
    }

    public void invalidateVerificationCodes(UUID userId) {
        jdbcTemplate.update("UPDATE email_verification_codes SET used_at = CURRENT_TIMESTAMP WHERE user_id = ? AND used_at IS NULL", userId);
    }

    public void createVerificationCode(UUID id, UUID userId, String hash, OffsetDateTime expiresAt) {
        jdbcTemplate.update("INSERT INTO email_verification_codes (id, user_id, code_hash, expires_at) VALUES (?, ?, ?, ?)", id, userId, hash, expiresAt);
    }

    public boolean consumeVerificationCode(UUID userId, String hash) {
        return jdbcTemplate.update("""
                UPDATE email_verification_codes SET used_at = CURRENT_TIMESTAMP
                 WHERE id = (SELECT id FROM email_verification_codes
                              WHERE user_id = ? AND code_hash = ? AND used_at IS NULL
                                AND expires_at > CURRENT_TIMESTAMP ORDER BY created_at DESC LIMIT 1)
                """, userId, hash) == 1;
    }

    public void enqueuePasswordReset(UUID id, String email, String token) {
        jdbcTemplate.update("""
                INSERT INTO email_outbox (id, recipient, template_key, payload)
                VALUES (?, ?, 'password-reset', jsonb_build_object('token', CAST(? AS TEXT)))
                """, id, email, token);
    }

    public void enqueueVerification(UUID id, String email, String code) {
        jdbcTemplate.update("""
                INSERT INTO email_outbox (id, recipient, template_key, payload)
                VALUES (?, ?, 'email-verification', jsonb_build_object('code', CAST(? AS TEXT)))
                """, id, email, code);
    }

    public void verifyUser(UUID userId) {
        jdbcTemplate.update("UPDATE users SET email_verified = TRUE, updated_at = CURRENT_TIMESTAMP WHERE id = ?", userId);
    }

    public void createSupportRequest(UUID id, UUID userId, String email, String message) {
        jdbcTemplate.update("INSERT INTO support_requests (id, user_id, email, message) VALUES (?, ?, ?, ?)", id, userId, email, message);
    }
}
