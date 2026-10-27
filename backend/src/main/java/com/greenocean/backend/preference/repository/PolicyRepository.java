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
    public java.util.List<PublishedPolicyResponse> history(String key,String locale) {
        return jdbcTemplate.query("""
          SELECT * FROM policy_documents WHERE policy_key=? AND locale=? AND status IN ('PUBLISHED','RETIRED')
          AND published_at<=CURRENT_TIMESTAMP AND content IS NOT NULL
          AND (effective_at IS NULL OR effective_at<=CURRENT_TIMESTAMP) ORDER BY version DESC LIMIT 100
          """,(rs,n)->new PublishedPolicyResponse(rs.getString("policy_key"),rs.getInt("version"),rs.getString("title"),rs.getString("summary"),rs.getString("content"),rs.getObject("published_at",OffsetDateTime.class).toInstant(),rs.getString("locale"),rs.getObject("effective_at",OffsetDateTime.class)==null?null:rs.getObject("effective_at",OffsetDateTime.class).toInstant(),rs.getString("review_status"),rs.getString("status"),rs.getObject("id",java.util.UUID.class)),key,locale);
    }

    public Optional<PublishedPolicyResponse> latestPublished(String key) {
        return latestPublished(key,"en");
    }
    public Optional<PublishedPolicyResponse> latestPublished(String key,String locale) {
        return jdbcTemplate.query("""
                SELECT id, policy_key, version, title, summary, content, published_at, locale, effective_at, review_status, status
                  FROM policy_documents
                 WHERE policy_key = ? AND locale = ? AND status = 'PUBLISHED' AND content IS NOT NULL
                   AND published_at IS NOT NULL AND published_at <= CURRENT_TIMESTAMP
                   AND (effective_at IS NULL OR effective_at <= CURRENT_TIMESTAMP)
                 ORDER BY version DESC LIMIT 1
                """, (rs, row) -> new PublishedPolicyResponse(rs.getString("policy_key"), rs.getInt("version"),
                rs.getString("title"), rs.getString("summary"), rs.getString("content"),
                rs.getObject("published_at", OffsetDateTime.class).toInstant(),rs.getString("locale"),
                rs.getObject("effective_at",OffsetDateTime.class)==null?null:rs.getObject("effective_at",OffsetDateTime.class).toInstant(),
                rs.getString("review_status"),rs.getString("status"),rs.getObject("id",java.util.UUID.class)), key,locale).stream().findFirst();
    }
}
