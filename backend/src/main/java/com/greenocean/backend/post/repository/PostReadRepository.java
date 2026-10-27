package com.greenocean.backend.post.repository;

import com.greenocean.backend.post.dto.AuthorSummary;
import com.greenocean.backend.post.dto.CommentResponse;
import com.greenocean.backend.post.dto.PostResponse;
import com.greenocean.backend.post.dto.ProfessionalReplyResponse;
import com.greenocean.backend.post.dto.SupportCategoryResponse;
import com.greenocean.backend.post.entity.PostType;
import com.greenocean.backend.post.entity.PostVisibility;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PostReadRepository {
    private static final String POST_SELECT = """
            SELECT p.author_id = ? AS owned,p.id, p.author_id, p.community_id, p.body, p.is_anonymous, p.visibility,
                   p.content_warning, p.category_id, p.post_type, p.mood, p.created_at, p.revision,p.status,p.safety_outcome,p.safety_reason,
                   pr.username, pr.display_name, pr.avatar_url,
                   sc.slug AS category_slug, sc.category_group, sc.name AS category_name,
                   sc.description AS category_description, sc.icon AS category_icon,
                   sc.color AS category_color, sc.soft_color AS category_soft_color,
                   0 AS category_post_count,
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

    private static final String ACCESS_PREDICATE = " AND can_read_social_post(p.id, ?) ";
    private static final String DISCOVERY_FILTER = """
      AND NOT EXISTS (SELECT 1 FROM user_preferences pref WHERE pref.user_id=? AND (
        (pref.stronger_content_controls AND p.content_warning IS NOT NULL)
        OR (pref.reduce_medication_content AND (LOWER(p.body) LIKE '%medication%' OR sc.slug LIKE '%medication%'))
        OR EXISTS (SELECT 1 FROM unnest(pref.muted_terms) term WHERE POSITION(LOWER(term) IN LOWER(p.body)) > 0)))
      """;

    private final JdbcTemplate jdbcTemplate;

    public PostReadRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PostResponse> findDiscovery(UUID viewer,int size,String mode,UUID category,java.time.Instant before,UUID beforeId) {
        var args=new java.util.ArrayList<Object>();args.add(viewer);args.add(viewer);args.add(viewer);
        String sql=POST_SELECT+" WHERE p.status='PUBLISHED' "+ACCESS_PREDICATE+DISCOVERY_FILTER;
        args.add(viewer);args.add(viewer);
        if(mode.equals("FOLLOWING")) {sql+=" AND NOT p.is_anonymous AND EXISTS(SELECT 1 FROM follows f WHERE f.follower_id=? AND f.following_id=p.author_id)";args.add(viewer);}
        if(mode.equals("SAVED")) {sql+=" AND EXISTS(SELECT 1 FROM bookmarks b WHERE b.user_id=? AND b.post_id=p.id)";args.add(viewer);}
        if(category!=null){sql+=" AND p.category_id=?";args.add(category);}
        if(before!=null){sql+=" AND (p.created_at,p.id)<(?::timestamptz,?::uuid)";args.add(before.toString());args.add(beforeId);}
        sql+=" ORDER BY p.created_at DESC,p.id DESC LIMIT ?";args.add(size+1);
        return jdbcTemplate.query(sql,this::mapPost,args.toArray());
    }
    public List<PostResponse> findFeed(UUID viewerId, int page, int size) {
        String sql = POST_SELECT + " WHERE p.status = 'PUBLISHED' " + ACCESS_PREDICATE + DISCOVERY_FILTER
                + " ORDER BY p.created_at DESC, p.id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, this::mapPost,
                viewerId, viewerId, viewerId, viewerId, viewerId,
                size + 1, (long) page * size);
    }

    public Optional<PostResponse> findOwnedPost(UUID id,UUID owner) {
        return jdbcTemplate.query(POST_SELECT+" WHERE p.id=? AND p.author_id=? AND p.deleted_at IS NULL",this::mapPost,owner,owner,owner,id,owner).stream().findFirst();
    }
    public List<PostResponse> findOwned(UUID owner,int page,int size) {
        return jdbcTemplate.query(POST_SELECT+" WHERE p.author_id=? AND p.deleted_at IS NULL ORDER BY p.created_at DESC,p.id DESC LIMIT ? OFFSET ?",this::mapPost,owner,owner,owner,owner,size+1,(long)page*size);
    }
    public Optional<CommentResponse> findOwnedComment(UUID id,UUID owner) {
        return jdbcTemplate.query(commentSelect()+" WHERE c.id=? AND c.author_id=? AND c.deleted_at IS NULL",this::mapComment,owner,owner,id,owner).stream().findFirst();
    }
    public Optional<PostResponse> findAccessiblePost(UUID postId, UUID viewerId) {
        String sql = POST_SELECT + " WHERE p.id = ? AND p.status = 'PUBLISHED' " + ACCESS_PREDICATE;
        List<PostResponse> posts = jdbcTemplate.query(sql, this::mapPost,
                viewerId, viewerId, viewerId, postId, viewerId);
        return posts.stream().findFirst();
    }

    public List<PostResponse> searchPosts(UUID viewerId, String query, int page, int size) {
        String sql = POST_SELECT + " WHERE p.status = 'PUBLISHED' AND LOWER(p.body) LIKE ? " + ACCESS_PREDICATE + DISCOVERY_FILTER
                + " ORDER BY p.created_at DESC, p.id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, this::mapPost,
                viewerId, viewerId, viewerId, "%" + query.toLowerCase() + "%",
                viewerId, viewerId,
                size + 1, (long) page * size);
    }

    public List<PostResponse> findCommunityPosts(UUID communityId, UUID viewerId, int page, int size) {
        String sql = POST_SELECT + " WHERE p.community_id = ? AND p.status = 'PUBLISHED' " + ACCESS_PREDICATE + DISCOVERY_FILTER
                + " ORDER BY p.created_at DESC, p.id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, this::mapPost,
                viewerId, viewerId, viewerId, communityId,
                viewerId, viewerId,
                size + 1, (long) page * size);
    }

    public List<PostResponse> findAuthorPosts(UUID authorId, UUID viewerId, int page, int size) {
        String sql = POST_SELECT + " WHERE p.author_id = ? AND p.status = 'PUBLISHED' AND (NOT p.is_anonymous OR p.author_id = ?) " + ACCESS_PREDICATE + DISCOVERY_FILTER
                + " ORDER BY p.created_at DESC, p.id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, this::mapPost,
                viewerId, viewerId, viewerId, authorId, viewerId,
                viewerId, viewerId,
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
                viewerId, viewerId, postId, viewerId, viewerId, size + 1, (long) page * size);
    }

    public Optional<CommentResponse> findComment(UUID commentId, UUID viewerId) {
        String sql = commentSelect() + """
                 WHERE c.id = ? AND c.status = 'PUBLISHED'
                """ + commentBlockPredicate();
        return jdbcTemplate.query(sql, this::mapComment, viewerId, viewerId, commentId, viewerId, viewerId)
                .stream().findFirst();
    }

    private String commentSelect() {
        return """
                SELECT c.author_id = ? AS owned,c.id, c.post_id, c.parent_comment_id, c.author_id, c.body, c.is_anonymous, c.created_at,c.revision,c.status,c.safety_outcome,c.safety_reason,
                       pr.username, pr.display_name, pr.avatar_url,
                       (SELECT COUNT(*) FROM comment_likes cl WHERE cl.comment_id = c.id) AS like_count,
                       EXISTS(SELECT 1 FROM comment_likes cl WHERE cl.comment_id = c.id AND cl.user_id = ?) AS liked
                  FROM comments c
                  JOIN profiles pr ON pr.user_id = c.author_id
                """;
    }

    private String commentBlockPredicate() {
        return """
                   AND EXISTS(SELECT 1 FROM users cu WHERE cu.id=c.author_id AND cu.status='ACTIVE')
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
                resultSet.getObject("created_at", OffsetDateTime.class).toInstant(),
                resultSet.getLong("revision"),resultSet.getString("status"),resultSet.getString("safety_outcome"),resultSet.getString("safety_reason"),resultSet.getBoolean("owned")
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
                resultSet.getObject("created_at", OffsetDateTime.class).toInstant(),
                resultSet.getLong("revision"),resultSet.getString("status"),resultSet.getString("safety_outcome"),resultSet.getString("safety_reason"),resultSet.getBoolean("owned")
        );
    }
}
