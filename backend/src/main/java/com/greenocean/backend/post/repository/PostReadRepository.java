package com.greenocean.backend.post.repository;

import com.greenocean.backend.post.dto.AuthorSummary;
import com.greenocean.backend.post.dto.CommentResponse;
import com.greenocean.backend.post.dto.PostResponse;
import com.greenocean.backend.post.dto.ProfessionalReplyResponse;
import com.greenocean.backend.post.dto.SupportCategoryResponse;
import com.greenocean.backend.post.entity.PostType;
import com.greenocean.backend.post.entity.FeedMode;
import com.greenocean.backend.post.entity.PostVisibility;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PostReadRepository {
    private static final String POST_SELECT = """
            SELECT p.id, p.author_id, p.community_id, p.body, p.is_anonymous, p.visibility,
                   p.content_warning, p.category_id, p.post_type, p.mood, p.created_at,
                   pr.username, pr.display_name, pr.avatar_url,
                   sc.slug AS category_slug, sc.category_group, sc.name AS category_name,
                   sc.description AS category_description, sc.icon AS category_icon,
                   sc.color AS category_color, sc.soft_color AS category_soft_color,
                   (SELECT COUNT(*) FROM posts category_posts
                     WHERE category_posts.category_id = sc.id AND category_posts.status = 'PUBLISHED') AS category_post_count,
                   pa.id AS professional_answer_id, pa.professional_user_id, pa.body AS professional_answer_body,
                   pa.helpful_count AS professional_answer_helpful_count, pa.created_at AS professional_answer_created_at,
                   pap.username AS professional_username, pap.display_name AS professional_display_name,
                   pap.avatar_url AS professional_avatar_url, pprof.title AS professional_title,
                   (SELECT COUNT(*) FROM post_likes pl WHERE pl.post_id = p.id) AS like_count,
                   (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id AND c.status = 'PUBLISHED') AS comment_count,
                   EXISTS(SELECT 1 FROM post_likes pl WHERE pl.post_id = p.id AND pl.user_id = ?) AS liked,
                   EXISTS(SELECT 1 FROM bookmarks b WHERE b.post_id = p.id AND b.user_id = ?) AS bookmarked
              FROM posts p
              JOIN profiles pr ON pr.user_id = p.author_id
              LEFT JOIN support_categories sc ON sc.id = p.category_id
              LEFT JOIN professional_answers pa ON pa.post_id = p.id
              LEFT JOIN profiles pap ON pap.user_id = pa.professional_user_id
              LEFT JOIN professional_profiles pprof ON pprof.user_id = pa.professional_user_id
            """;

    private static final String ACCESS_PREDICATE = """
               AND NOT EXISTS (
                   SELECT 1 FROM blocks b
                    WHERE (b.blocker_id = ? AND b.blocked_id = p.author_id)
                       OR (b.blocker_id = p.author_id AND b.blocked_id = ?)
               )
               AND (
                   p.author_id = ?
                   OR p.visibility = 'PUBLIC'
                   OR (p.visibility = 'FOLLOWERS' AND EXISTS (
                       SELECT 1 FROM follows f WHERE f.follower_id = ? AND f.following_id = p.author_id
                   ))
                   OR (p.visibility = 'COMMUNITY' AND EXISTS (
                       SELECT 1 FROM community_members cm WHERE cm.community_id = p.community_id AND cm.user_id = ?
                   ))
               )
            """;

    private final JdbcTemplate jdbcTemplate;

    public PostReadRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PostResponse> findFeed(UUID viewerId, FeedMode mode, int page, int size) {
        String modePredicate = switch (mode) {
            case FOR_YOU -> "";
            case FOLLOWING -> " AND EXISTS (SELECT 1 FROM follows feed_follow WHERE feed_follow.follower_id = ? AND feed_follow.following_id = p.author_id) ";
            case PROFESSIONAL_ANSWERS -> " AND EXISTS (SELECT 1 FROM professional_answers feed_answer WHERE feed_answer.post_id = p.id) ";
        };
        String sql = POST_SELECT + " WHERE p.status = 'PUBLISHED' " + modePredicate + ACCESS_PREDICATE
                + " ORDER BY p.created_at DESC, p.id DESC LIMIT ? OFFSET ?";
        List<Object> parameters = new ArrayList<>();
        parameters.add(viewerId);
        parameters.add(viewerId);
        if (mode == FeedMode.FOLLOWING) parameters.add(viewerId);
        parameters.add(viewerId);
        parameters.add(viewerId);
        parameters.add(viewerId);
        parameters.add(viewerId);
        parameters.add(viewerId);
        parameters.add(size + 1);
        parameters.add((long) page * size);
        return jdbcTemplate.query(sql, this::mapPost, parameters.toArray());
    }

    public Optional<PostResponse> findAccessiblePost(UUID postId, UUID viewerId) {
        String sql = POST_SELECT + " WHERE p.id = ? AND p.status = 'PUBLISHED' " + ACCESS_PREDICATE;
        List<PostResponse> posts = jdbcTemplate.query(sql, this::mapPost,
                viewerId, viewerId, postId, viewerId, viewerId, viewerId, viewerId, viewerId);
        return posts.stream().findFirst();
    }

    public List<PostResponse> searchPosts(UUID viewerId, String query, int page, int size) {
        String sql = POST_SELECT + " WHERE p.status = 'PUBLISHED' AND LOWER(p.body) LIKE ? " + ACCESS_PREDICATE
                + " ORDER BY p.created_at DESC, p.id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, this::mapPost,
                viewerId, viewerId, "%" + query.toLowerCase() + "%",
                viewerId, viewerId, viewerId, viewerId, viewerId,
                size + 1, (long) page * size);
    }

    public List<PostResponse> findCommunityPosts(UUID communityId, UUID viewerId, int page, int size) {
        String sql = POST_SELECT + " WHERE p.community_id = ? AND p.status = 'PUBLISHED' " + ACCESS_PREDICATE
                + " ORDER BY p.created_at DESC, p.id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, this::mapPost,
                viewerId, viewerId, communityId,
                viewerId, viewerId, viewerId, viewerId, viewerId,
                size + 1, (long) page * size);
    }

    public List<CommentResponse> findComments(UUID postId, UUID viewerId, int page, int size) {
        String sql = commentSelect() + """
                 WHERE c.post_id = ? AND c.status = 'PUBLISHED'
                """ + commentBlockPredicate() + """
                 ORDER BY c.created_at ASC, c.id ASC
                 LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, this::mapComment,
                viewerId, postId, viewerId, viewerId, size + 1, (long) page * size);
    }

    public Optional<CommentResponse> findComment(UUID commentId, UUID viewerId) {
        String sql = commentSelect() + """
                 WHERE c.id = ? AND c.status = 'PUBLISHED'
                """ + commentBlockPredicate();
        return jdbcTemplate.query(sql, this::mapComment, viewerId, commentId, viewerId, viewerId)
                .stream().findFirst();
    }

    private String commentSelect() {
        return """
                SELECT c.id, c.post_id, c.parent_comment_id, c.author_id, c.body, c.is_anonymous, c.created_at,
                       pr.username, pr.display_name, pr.avatar_url,
                       (SELECT COUNT(*) FROM comment_likes cl WHERE cl.comment_id = c.id) AS like_count,
                       EXISTS(SELECT 1 FROM comment_likes cl WHERE cl.comment_id = c.id AND cl.user_id = ?) AS liked
                  FROM comments c
                  JOIN profiles pr ON pr.user_id = c.author_id
                """;
    }

    private String commentBlockPredicate() {
        return """
                   AND NOT EXISTS (
                       SELECT 1 FROM blocks b
                        WHERE (b.blocker_id = ? AND b.blocked_id = c.author_id)
                           OR (b.blocker_id = c.author_id AND b.blocked_id = ?)
                   )
                """;
    }

    private PostResponse mapPost(ResultSet resultSet, int rowNumber) throws SQLException {
        boolean anonymous = resultSet.getBoolean("is_anonymous");
        AuthorSummary author = anonymous ? null : new AuthorSummary(
                resultSet.getObject("author_id", UUID.class),
                resultSet.getString("username"),
                resultSet.getString("display_name"),
                resultSet.getString("avatar_url")
        );
        UUID categoryId = resultSet.getObject("category_id", UUID.class);
        SupportCategoryResponse category = categoryId == null ? null : new SupportCategoryResponse(
                categoryId, resultSet.getString("category_slug"), resultSet.getString("category_group"),
                resultSet.getString("category_name"), resultSet.getString("category_description"),
                resultSet.getString("category_icon"), resultSet.getString("category_color"),
                resultSet.getString("category_soft_color"), resultSet.getLong("category_post_count")
        );
        UUID answerId = resultSet.getObject("professional_answer_id", UUID.class);
        ProfessionalReplyResponse professionalReply = answerId == null ? null : new ProfessionalReplyResponse(
                answerId,
                new ProfessionalReplyResponse.ProfessionalAuthor(
                        resultSet.getObject("professional_user_id", UUID.class),
                        resultSet.getString("professional_username"), resultSet.getString("professional_display_name"),
                        resultSet.getString("professional_title"), resultSet.getString("professional_avatar_url")
                ),
                resultSet.getString("professional_answer_body"), resultSet.getLong("professional_answer_helpful_count"),
                resultSet.getObject("professional_answer_created_at", OffsetDateTime.class).toInstant()
        );
        return new PostResponse(
                resultSet.getObject("id", UUID.class), author,
                resultSet.getObject("community_id", UUID.class), resultSet.getString("body"), anonymous,
                PostVisibility.valueOf(resultSet.getString("visibility")), resultSet.getString("content_warning"),
                resultSet.getLong("like_count"), resultSet.getLong("comment_count"),
                resultSet.getBoolean("liked"), resultSet.getBoolean("bookmarked"),
                category, PostType.valueOf(resultSet.getString("post_type")), resultSet.getString("mood"), professionalReply,
                resultSet.getObject("created_at", OffsetDateTime.class).toInstant()
        );
    }

    private CommentResponse mapComment(ResultSet resultSet, int rowNumber) throws SQLException {
        boolean anonymous = resultSet.getBoolean("is_anonymous");
        AuthorSummary author = anonymous ? null : new AuthorSummary(
                resultSet.getObject("author_id", UUID.class), resultSet.getString("username"),
                resultSet.getString("display_name"), resultSet.getString("avatar_url")
        );
        return new CommentResponse(
                resultSet.getObject("id", UUID.class), resultSet.getObject("post_id", UUID.class),
                resultSet.getObject("parent_comment_id", UUID.class), author, resultSet.getString("body"), anonymous,
                resultSet.getLong("like_count"), resultSet.getBoolean("liked"),
                resultSet.getObject("created_at", OffsetDateTime.class).toInstant()
        );
    }
}
