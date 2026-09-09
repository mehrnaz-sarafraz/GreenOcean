package com.greenocean.backend.auth.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SecurityAuditService {
    private final JdbcTemplate jdbcTemplate;

    public SecurityAuditService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void record(UUID actorId, String action, String entityType, UUID entityId) {
        jdbcTemplate.update("""
                INSERT INTO audit_logs (actor_user_id, action, entity_type, entity_id)
                VALUES (?, ?, ?, ?)
                """, actorId, action, entityType, entityId);
    }

    public void recordForUser(UUID userId, String action) {
        record(userId, action, "USER", userId);
    }

    public void recordAnonymous(String action) {
        record(null, action, "AUTH", null);
    }
}
