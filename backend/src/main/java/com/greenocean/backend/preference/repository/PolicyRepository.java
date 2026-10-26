package com.greenocean.backend.preference.repository;

import com.greenocean.backend.preference.dto.PublishedPolicyResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class PolicyRepository {
    private final JdbcTemplate jdbcTemplate;

    public PolicyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<PublishedPolicyResponse> latestPublished(String key) {
        return jdbcTemplate.query("""
                SELECT policy_key, version, title, summary, content, published_at
                  FROM policy_documents
                 WHERE policy_key = ? AND status = 'PUBLISHED' AND content IS NOT NULL
                   AND published_at IS NOT NULL AND published_at <= CURRENT_TIMESTAMP
                 ORDER BY version DESC LIMIT 1
                """, (rs, row) -> new PublishedPolicyResponse(rs.getString("policy_key"), rs.getInt("version"),
                rs.getString("title"), rs.getString("summary"), rs.getString("content"),
                rs.getObject("published_at", OffsetDateTime.class).toInstant()), key).stream().findFirst();
    }
}
