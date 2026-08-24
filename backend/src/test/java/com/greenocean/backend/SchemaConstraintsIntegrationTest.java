package com.greenocean.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SchemaConstraintsIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldEnforceCoreSchemaInvariants() {
        assertDoesNotThrow(() -> jdbcTemplate.execute("""
            DO $$
            DECLARE
                first_user_id UUID := uuidv7();
                second_user_id UUID := uuidv7();
                community_id UUID := uuidv7();
                post_id UUID := uuidv7();
                parent_comment_id UUID := uuidv7();
            BEGIN
                INSERT INTO users (id, email, password_hash)
                VALUES
                    (first_user_id, 'schema-test-one@example.test', 'not-a-real-password-hash'),
                    (second_user_id, 'schema-test-two@example.test', 'not-a-real-password-hash');

                BEGIN
                    INSERT INTO users (id, email, password_hash)
                    VALUES (uuidv7(), 'SCHEMA-TEST-ONE@example.test', 'not-a-real-password-hash');
                    RAISE EXCEPTION 'case-insensitive email uniqueness was not enforced';
                EXCEPTION WHEN unique_violation THEN
                    NULL;
                END;

                BEGIN
                    INSERT INTO follows (follower_id, following_id)
                    VALUES (first_user_id, first_user_id);
                    RAISE EXCEPTION 'self-follow was not rejected';
                EXCEPTION WHEN check_violation THEN
                    NULL;
                END;

                INSERT INTO communities (id, name, slug, created_by)
                VALUES (community_id, 'Schema Test Community', 'schema-test-community', first_user_id);

                BEGIN
                    INSERT INTO posts (id, author_id, visibility, body)
                    VALUES (uuidv7(), first_user_id, 'COMMUNITY', 'Missing community id');
                    RAISE EXCEPTION 'community visibility without community_id was not rejected';
                EXCEPTION WHEN check_violation THEN
                    NULL;
                END;

                INSERT INTO posts (id, author_id, community_id, visibility, body)
                VALUES (post_id, first_user_id, community_id, 'COMMUNITY', 'A valid community post');

                INSERT INTO comments (id, post_id, author_id, body)
                VALUES (parent_comment_id, post_id, first_user_id, 'A parent comment');

                INSERT INTO comments (post_id, author_id, parent_comment_id, body)
                VALUES (post_id, second_user_id, parent_comment_id, 'A valid reply');

                INSERT INTO post_likes (post_id, user_id)
                VALUES (post_id, second_user_id);

                BEGIN
                    INSERT INTO post_likes (post_id, user_id)
                    VALUES (post_id, second_user_id);
                    RAISE EXCEPTION 'duplicate post like was not rejected';
                EXCEPTION WHEN unique_violation THEN
                    NULL;
                END;

                BEGIN
                    INSERT INTO reports (reporter_id, target_user_id, post_id, reason)
                    VALUES (second_user_id, first_user_id, post_id, 'SPAM');
                    RAISE EXCEPTION 'multiple report targets were not rejected';
                EXCEPTION WHEN check_violation THEN
                    NULL;
                END;

                BEGIN
                    INSERT INTO professional_verifications (professional_user_id, status)
                    VALUES (first_user_id, 'PENDING');
                    RAISE EXCEPTION 'verification without professional profile was not rejected';
                EXCEPTION WHEN foreign_key_violation THEN
                    NULL;
                END;
            END;
            $$;
            """));
    }
}
