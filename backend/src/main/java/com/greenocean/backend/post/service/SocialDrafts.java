package com.greenocean.backend.post.service;

import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.post.dto.CreatePostRequest;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.util.*;

@Service
public class SocialDrafts {
    public record Payload(@NotNull @Size(max=10000) String body,
            @Pattern(regexp="ANONYMOUS|SOCIAL_IDENTITY") String publicationIdentity,
            @Pattern(regexp="FOLLOWERS|COMMUNITY|PUBLIC") String visibility,
            UUID communityId, @Size(max=120) String contentWarning, UUID categoryId,
            @Pattern(regexp="EXPERIENCE|QUESTION|REFLECTION") String postType, @Size(max=30) String mood) {}
    public record Save(@NotNull @jakarta.validation.Valid Payload payload, @Min(0) long revision) {}
    private final JdbcTemplate db;
    private final ObjectMapper json;
    private final PostService posts;
    public SocialDrafts(JdbcTemplate db,ObjectMapper json,PostService posts) {this.db=db;this.json=json;this.posts=posts;}
    @Transactional
    public Map<String,Object> save(UUID owner, UUID id, Save request) {
        db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))","draft-owner:"+owner);
        String body=json.writeValueAsString(request.payload());
        if(id==null) {
            Long count=db.queryForObject("SELECT COUNT(*) FROM social_drafts WHERE owner_id=? AND expires_at>CURRENT_TIMESTAMP",Long.class,owner);
            if(count!=null && count>=20)throw new ConflictException("Discard an old draft before creating another");
            id=UUID.randomUUID();
            db.update("INSERT INTO social_drafts(id,owner_id,payload) VALUES (?,?,?::jsonb)",id,owner,body);
        } else if(db.update("UPDATE social_drafts SET payload=?::jsonb,revision=revision+1,updated_at=CURRENT_TIMESTAMP,expires_at=CURRENT_TIMESTAMP+INTERVAL '7 days' WHERE id=? AND owner_id=? AND revision=? AND expires_at>CURRENT_TIMESTAMP",body,id,owner,request.revision())!=1)
            throw new ConflictException("Draft unavailable or changed; reload before saving");
        return get(owner,id);
    }
    public List<Map<String,Object>> list(UUID owner) {
        return db.queryForList("SELECT id,payload::text AS payload,revision,updated_at,expires_at FROM social_drafts WHERE owner_id=? AND expires_at>CURRENT_TIMESTAMP ORDER BY updated_at DESC,id DESC LIMIT 20",owner);
    }
    public Map<String,Object> get(UUID owner,UUID id) {
        return db.queryForList("SELECT id,payload::text AS payload,revision,updated_at,expires_at FROM social_drafts WHERE id=? AND owner_id=? AND expires_at>CURRENT_TIMESTAMP",id,owner).stream().findFirst().orElseThrow(()->new NotFoundException("Draft was not found"));
    }
    @Transactional
    public void discard(UUID owner,UUID id) { db.update("DELETE FROM social_drafts WHERE id=? AND owner_id=?",id,owner); }
    @Transactional
    public com.greenocean.backend.post.dto.PostResponse publish(UUID owner,UUID id,CreatePostRequest request) {
        var rows=db.queryForList("SELECT id FROM social_drafts WHERE id=? AND owner_id=? AND expires_at>CURRENT_TIMESTAMP FOR UPDATE",id,owner);
        if(rows.isEmpty())throw new NotFoundException("Draft was not found");
        var response=posts.create(owner,request);
        discard(owner,id);
        return response;
    }
    @Scheduled(fixedDelayString="${greenocean.social.draft-cleanup-ms:60000}")
    @Transactional
    public void expire() {
        db.update("DELETE FROM social_drafts WHERE id IN (SELECT id FROM social_drafts WHERE expires_at<=CURRENT_TIMESTAMP ORDER BY expires_at LIMIT 200 FOR UPDATE SKIP LOCKED)");
    }
}
