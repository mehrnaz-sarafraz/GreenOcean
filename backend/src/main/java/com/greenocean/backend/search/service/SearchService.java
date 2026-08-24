package com.greenocean.backend.search.service;

import com.greenocean.backend.common.api.PageResponse;
import com.greenocean.backend.post.dto.PostResponse;
import com.greenocean.backend.post.repository.PostReadRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SearchService {
    private final JdbcTemplate jdbcTemplate;
    private final PostReadRepository postReadRepository;

    public SearchService(JdbcTemplate jdbcTemplate, PostReadRepository postReadRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.postReadRepository = postReadRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSearchResponse> users(UUID viewerId, String query, int page, int size) {
        String term = "%" + query.trim().toLowerCase() + "%";
        List<UserSearchResponse> results = jdbcTemplate.query("""
                SELECT p.user_id, p.username, p.display_name, p.avatar_url, p.bio, p.is_profile_private,
                       EXISTS(SELECT 1 FROM follows f WHERE f.follower_id = ? AND f.following_id = p.user_id) AS following,
                       (SELECT COUNT(*) FROM follows f WHERE f.following_id = p.user_id) AS follower_count
                  FROM profiles p
                 WHERE p.user_id <> ?
                   AND (LOWER(p.username) LIKE ? OR LOWER(p.display_name) LIKE ?)
                   AND NOT EXISTS (
                       SELECT 1 FROM blocks b
                        WHERE (b.blocker_id = ? AND b.blocked_id = p.user_id)
                           OR (b.blocker_id = p.user_id AND b.blocked_id = ?)
                   )
                 ORDER BY CASE WHEN LOWER(p.username) = ? THEN 0 ELSE 1 END, p.username
                 LIMIT ? OFFSET ?
                """, (rs, row) -> new UserSearchResponse(
                        rs.getObject("user_id", UUID.class), rs.getString("username"), rs.getString("display_name"),
                        rs.getString("avatar_url"), rs.getString("bio"), rs.getBoolean("is_profile_private"),
                        rs.getBoolean("following"), rs.getLong("follower_count")
                ), viewerId, viewerId, term, term, viewerId, viewerId, query.trim().toLowerCase(),
                size + 1, (long) page * size);
        return page(results, page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> posts(UUID viewerId, String query, int page, int size) {
        List<PostResponse> results = postReadRepository.searchPosts(viewerId, query.trim(), page, size);
        return page(results, page, size);
    }

    private <T> PageResponse<T> page(List<T> results, int page, int size) {
        boolean hasNext = results.size() > size;
        return new PageResponse<>(hasNext ? List.copyOf(results.subList(0, size)) : List.copyOf(results), page, size, hasNext);
    }
}
