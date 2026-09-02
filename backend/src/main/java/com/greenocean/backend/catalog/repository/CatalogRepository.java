package com.greenocean.backend.catalog.repository;

import com.greenocean.backend.catalog.dto.ArticleResponse;
import com.greenocean.backend.catalog.dto.CreateArticleRequest;
import com.greenocean.backend.catalog.dto.MediaRecommendationResponse;
import com.greenocean.backend.catalog.dto.ProfessionalResponse;
import com.greenocean.backend.post.dto.SupportCategoryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CatalogRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public CatalogRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public List<SupportCategoryResponse> categories() {
        return jdbcTemplate.query("""
                SELECT sc.id, sc.slug, sc.category_group, sc.name, sc.description, sc.icon, sc.color, sc.soft_color,
                       (SELECT COUNT(*) FROM posts p WHERE p.category_id = sc.id AND p.status = 'PUBLISHED') AS post_count
                  FROM support_categories sc
                 WHERE sc.is_active = TRUE
                 ORDER BY sc.sort_order, sc.name
                """, (rs, row) -> new SupportCategoryResponse(
                rs.getObject("id", UUID.class), rs.getString("slug"), rs.getString("category_group"),
                rs.getString("name"), rs.getString("description"), rs.getString("icon"),
                rs.getString("color"), rs.getString("soft_color"), rs.getLong("post_count")));
    }

    public List<ProfessionalResponse> professionals(UUID viewerId, String query, String specialty, boolean availableOnly) {
        String search = "%" + query.trim().toLowerCase() + "%";
        String specialtySearch = specialty == null || specialty.isBlank() || "all".equalsIgnoreCase(specialty)
                ? null : specialty.trim().toLowerCase();
        return jdbcTemplate.query("""
                SELECT pp.*, p.username, p.display_name, p.avatar_url,
                       EXISTS(SELECT 1 FROM follows f WHERE f.follower_id = ? AND f.following_id = pp.user_id) AS followed
                  FROM professional_profiles pp
                  JOIN profiles p ON p.user_id = pp.user_id
                 WHERE pp.verification_status = 'VERIFIED'
                   AND (? = '%%' OR LOWER(CONCAT_WS(' ', p.display_name, p.username, pp.title, pp.specialization,
                         pp.city, pp.country, array_to_string(pp.specialties, ' '), array_to_string(pp.languages, ' '))) LIKE ?)
                   AND (CAST(? AS VARCHAR) IS NULL OR EXISTS (SELECT 1 FROM unnest(pp.specialties) value WHERE LOWER(value) LIKE '%' || CAST(? AS VARCHAR) || '%'))
                   AND (? = FALSE OR pp.accepting_new_clients = TRUE)
                 ORDER BY pp.promoted DESC, pp.green_ocean_score DESC, pp.rating DESC, p.display_name
                """, this::mapProfessional, viewerId, search, search, specialtySearch, specialtySearch, availableOnly);
    }

    public Optional<ProfessionalResponse> professional(UUID id, UUID viewerId) {
        return jdbcTemplate.query("""
                SELECT pp.*, p.username, p.display_name, p.avatar_url,
                       EXISTS(SELECT 1 FROM follows f WHERE f.follower_id = ? AND f.following_id = pp.user_id) AS followed
                  FROM professional_profiles pp
                  JOIN profiles p ON p.user_id = pp.user_id
                 WHERE pp.user_id = ? AND pp.verification_status = 'VERIFIED'
                """, this::mapProfessional, viewerId, id).stream().findFirst();
    }

    public boolean isVerifiedProfessional(UUID userId) {
        Boolean found = jdbcTemplate.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM professional_profiles
                               WHERE user_id = ? AND verification_status = 'VERIFIED')
                """, Boolean.class, userId);
        return Boolean.TRUE.equals(found);
    }

    public List<ArticleResponse> articles(UUID viewerId, String topic) {
        String topicFilter = topic == null || topic.isBlank() || "all".equalsIgnoreCase(topic) ? null : topic.trim();
        return jdbcTemplate.query(articleSelect() + """
                 WHERE a.status = 'PUBLISHED' AND (CAST(? AS VARCHAR) IS NULL OR LOWER(a.topic) = LOWER(CAST(? AS VARCHAR)))
                 ORDER BY a.pinned DESC, a.published_at DESC
                """, this::mapArticle, viewerId, viewerId, topicFilter, topicFilter);
    }

    public Optional<ArticleResponse> article(UUID viewerId, UUID articleId) {
        return jdbcTemplate.query(articleSelect() + " WHERE a.id = ? AND a.status = 'PUBLISHED'",
                this::mapArticle, viewerId, viewerId, articleId).stream().findFirst();
    }

    public void createArticle(UUID id, UUID authorId, CreateArticleRequest request) {
        String reference = request.references() == null || request.references().isBlank() ? null : request.references().trim();
        jdbcTemplate.update("""
                INSERT INTO professional_articles (
                    id, author_id, title, summary, topic, read_time_minutes, status, pinned, evidence_level,
                    sections, takeaways, reference_list
                ) VALUES (?, ?, ?, ?, ?, ?, 'IN_REVIEW', ?, 'Awaiting editorial review',
                    jsonb_build_array(jsonb_build_object('heading', 'Article', 'body', ?)),
                    '[]'::jsonb,
                    CASE WHEN ? IS NULL THEN '[]'::jsonb ELSE jsonb_build_array(?) END)
                """, id, authorId, request.title().trim(), request.summary().trim(), request.topic().trim(),
                estimatedReadTime(request.body()), request.pinned(), request.body().trim(), reference, reference);
    }

    public boolean markArticleHelpful(UUID articleId, UUID userId) {
        int inserted = jdbcTemplate.update("""
                INSERT INTO article_helpful_reactions (article_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING
                """, articleId, userId);
        if (inserted == 1) jdbcTemplate.update(
                "UPDATE professional_articles SET helpful_count = helpful_count + 1 WHERE id = ?", articleId);
        return inserted == 1;
    }

    public void removeArticleHelpful(UUID articleId, UUID userId) {
        int deleted = jdbcTemplate.update(
                "DELETE FROM article_helpful_reactions WHERE article_id = ? AND user_id = ?", articleId, userId);
        if (deleted == 1) jdbcTemplate.update(
                "UPDATE professional_articles SET helpful_count = GREATEST(0, helpful_count - 1) WHERE id = ?", articleId);
    }

    public void setArticleSaved(UUID articleId, UUID userId, boolean saved) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM professional_articles WHERE id = ? AND status = 'PUBLISHED')",
                Boolean.class, articleId);
        if (!Boolean.TRUE.equals(exists)) throw new IllegalArgumentException("Article was not found");
        if (saved) {
            jdbcTemplate.update("INSERT INTO article_bookmarks (article_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                    articleId, userId);
        } else {
            jdbcTemplate.update("DELETE FROM article_bookmarks WHERE article_id = ? AND user_id = ?", articleId, userId);
        }
    }

    public List<MediaRecommendationResponse> media(UUID viewerId, String kind) {
        String kindFilter = kind == null || kind.isBlank() || "all".equalsIgnoreCase(kind) ? null : kind.toUpperCase();
        return jdbcTemplate.query("""
                SELECT m.*, EXISTS(SELECT 1 FROM media_saves s WHERE s.media_id = m.id AND s.user_id = ?) AS saved
                  FROM media_recommendations m
                 WHERE m.is_published = TRUE AND (CAST(? AS VARCHAR) IS NULL OR m.media_kind = CAST(? AS VARCHAR))
                 ORDER BY m.created_at, m.title
                """, (rs, row) -> new MediaRecommendationResponse(
                rs.getObject("id", UUID.class), rs.getString("title"), rs.getString("media_kind"),
                rs.getShort("release_year"), rs.getString("duration_label"), rs.getString("theme"),
                rs.getString("description"), rs.getString("discussion_prompt"), strings(rs.getArray("content_notes")),
                rs.getString("recommended_by"), rs.getString("accent"), rs.getString("soft_accent"), rs.getBoolean("saved")),
                viewerId, kindFilter, kindFilter);
    }

    public void setMediaSaved(UUID mediaId, UUID userId, boolean saved) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM media_recommendations WHERE id = ? AND is_published = TRUE)",
                Boolean.class, mediaId);
        if (!Boolean.TRUE.equals(exists)) throw new IllegalArgumentException("Media recommendation was not found");
        if (saved) {
            jdbcTemplate.update("INSERT INTO media_saves (media_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING", mediaId, userId);
        } else {
            jdbcTemplate.update("DELETE FROM media_saves WHERE media_id = ? AND user_id = ?", mediaId, userId);
        }
    }

    private String articleSelect() {
        return """
                SELECT a.*,
                       EXISTS(SELECT 1 FROM article_helpful_reactions h WHERE h.article_id = a.id AND h.user_id = ?) AS helpful,
                       EXISTS(SELECT 1 FROM article_bookmarks b WHERE b.article_id = a.id AND b.user_id = ?) AS saved
                  FROM professional_articles a
                """;
    }

    private ProfessionalResponse mapProfessional(ResultSet rs, int row) throws SQLException {
        return new ProfessionalResponse(
                rs.getObject("user_id", UUID.class), rs.getString("display_name"), rs.getString("username"),
                rs.getString("title"), strings(rs.getArray("specialties")), rs.getString("avatar_url"),
                rs.getBigDecimal("rating"), rs.getInt("review_count"), rs.getInt("green_ocean_score"),
                rs.getInt("years_of_experience"), strings(rs.getArray("languages")),
                "VERIFIED".equals(rs.getString("verification_status")), rs.getBoolean("promoted"),
                rs.getString("promoted_reason"), rs.getString("bio"), rs.getString("gender"),
                rs.getString("country"), rs.getString("city"), rs.getString("workplace"),
                rs.getString("clinic_name"), rs.getString("clinic_address"), strings(rs.getArray("education")),
                rs.getString("license_number"), strings(rs.getArray("consultation_modes")),
                rs.getBoolean("accepting_new_clients"), rs.getBoolean("followed"));
    }

    private ArticleResponse mapArticle(ResultSet rs, int row) throws SQLException {
        OffsetDateTime published = rs.getObject("published_at", OffsetDateTime.class);
        return new ArticleResponse(
                rs.getObject("id", UUID.class), rs.getObject("author_id", UUID.class), rs.getString("title"),
                rs.getString("summary"), rs.getString("topic"), rs.getInt("read_time_minutes") + " min read",
                rs.getString("status"), rs.getBoolean("pinned"), rs.getString("evidence_level"),
                json(rs.getString("sections")), json(rs.getString("takeaways")), json(rs.getString("reference_list")),
                rs.getLong("helpful_count"), rs.getBoolean("helpful"), rs.getBoolean("saved"),
                published == null ? null : published.toInstant());
    }

    private JsonNode json(String value) throws SQLException {
        try {
            return objectMapper.readTree(value);
        } catch (RuntimeException exception) {
            throw new SQLException("Stored article JSON is invalid", exception);
        }
    }

    private List<String> strings(Array array) throws SQLException {
        if (array == null) return List.of();
        return Arrays.asList((String[]) array.getArray());
    }

    private int estimatedReadTime(String body) {
        int words = body.trim().split("\\s+").length;
        return Math.max(1, Math.min(180, (int) Math.ceil(words / 220.0)));
    }
}
