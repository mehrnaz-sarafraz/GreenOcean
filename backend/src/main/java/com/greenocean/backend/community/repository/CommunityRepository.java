package com.greenocean.backend.community.repository;

import com.greenocean.backend.community.dto.CommunityResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CommunityRepository {
    private static final String SELECT = """
            SELECT c.id, c.name, c.slug, c.description, c.icon_url, c.is_private, c.created_at,
                   (SELECT COUNT(*) FROM community_members all_members WHERE all_members.community_id = c.id) AS member_count,
                   EXISTS(SELECT 1 FROM community_members mine WHERE mine.community_id = c.id AND mine.user_id = ?) AS member,
                   (SELECT mine.role FROM community_members mine WHERE mine.community_id = c.id AND mine.user_id = ?) AS membership_role
              FROM communities c
             WHERE c.status = 'ACTIVE'
            """;

    private final JdbcTemplate jdbcTemplate;

    public CommunityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(UUID id, UUID ownerId, String name, String slug, String description, boolean privateCommunity) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO communities (id, name, slug, description, is_private, created_by)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, id, name, slug, description, privateCommunity, ownerId);
            jdbcTemplate.update("""
                    INSERT INTO community_members (community_id, user_id, role)
                    VALUES (?, ?, 'OWNER')
                    """, id, ownerId);
        } catch (DuplicateKeyException exception) {
            throw new IllegalStateException("A community with this name or slug already exists", exception);
        }
    }

    public Optional<CommunityResponse> findById(UUID communityId, UUID viewerId) {
        return jdbcTemplate.query(SELECT + " AND c.id = ?", this::map,
                viewerId, viewerId, communityId).stream().findFirst();
    }

    public Optional<CommunityResponse> findBySlug(String slug, UUID viewerId) {
        return jdbcTemplate.query(SELECT + " AND c.slug = ?", this::map,
                viewerId, viewerId, slug).stream().findFirst();
    }

    public List<CommunityResponse> findAll(UUID viewerId, String query, int page, int size) {
        String term = "%" + query.trim().toLowerCase() + "%";
        String sql = SELECT + """
                 AND (LOWER(c.name) LIKE ? OR LOWER(COALESCE(c.description, '')) LIKE ? OR LOWER(c.slug) LIKE ?)
                 ORDER BY member DESC, member_count DESC, c.name
                 LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, this::map, viewerId, viewerId, term, term, term,
                size + 1, (long) page * size);
    }

    public int join(UUID communityId, UUID userId) {
        return jdbcTemplate.update("""
                INSERT INTO community_members (community_id, user_id, role)
                VALUES (?, ?, 'MEMBER')
                ON CONFLICT DO NOTHING
                """, communityId, userId);
    }

    public String membershipRole(UUID communityId, UUID userId) {
        List<String> roles = jdbcTemplate.query("""
                SELECT role FROM community_members WHERE community_id = ? AND user_id = ?
                """, (resultSet, row) -> resultSet.getString("role"), communityId, userId);
        return roles.stream().findFirst().orElse(null);
    }

    public void leave(UUID communityId, UUID userId) {
        jdbcTemplate.update("DELETE FROM community_members WHERE community_id = ? AND user_id = ?", communityId, userId);
    }

    private CommunityResponse map(ResultSet resultSet, int rowNumber) throws SQLException {
        return new CommunityResponse(
                resultSet.getObject("id", UUID.class), resultSet.getString("name"), resultSet.getString("slug"),
                resultSet.getString("description"), resultSet.getString("icon_url"), resultSet.getBoolean("is_private"),
                resultSet.getLong("member_count"), resultSet.getBoolean("member"), resultSet.getString("membership_role"),
                resultSet.getObject("created_at", OffsetDateTime.class).toInstant()
        );
    }
}
