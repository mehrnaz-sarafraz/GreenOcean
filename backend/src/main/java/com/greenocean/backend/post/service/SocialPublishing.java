package com.greenocean.backend.post.service;

import com.greenocean.backend.common.exception.ForbiddenException;
import com.greenocean.backend.common.exception.ConflictException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class SocialPublishing {
    private final JdbcTemplate db;
    private final ContentClassifier safety;
    private final com.greenocean.backend.auth.service.AuthRateLimit rateLimit;
    private final boolean enabled;
    private final boolean policyApproved;
    public SocialPublishing(JdbcTemplate db, ContentClassifier safety, com.greenocean.backend.auth.service.AuthRateLimit rateLimit,
            @Value("${greenocean.social.publishing-enabled:false}") boolean enabled,
            @Value("${greenocean.social.policy-approved:false}") boolean approved) {
        this.db=db; this.safety=safety; this.rateLimit=rateLimit; this.enabled=enabled; this.policyApproved=approved;
    }
    public ContentSafety.Assessment check(UUID owner, String action, String body, UUID excludedId) {
        if (!enabled || !policyApproved) throw new ForbiddenException("Community publishing is unavailable pending safety operations approval");
        if(!rateLimit.allowInCurrentTransaction("social-content:"+owner+":"+action,action.equals("POST")?10:30,java.time.Duration.ofMinutes(1)))
            throw new ConflictException("Please pause before submitting more content");
        db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))", "social-write:"+owner);
        String table = action.equals("POST") ? "posts" : "comments";
        Long recent=db.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE author_id=? AND created_at>CURRENT_TIMESTAMP-INTERVAL '1 minute'",Long.class,owner);
        if (recent != null && recent >= (action.equals("POST")?5:15))
            throw new ConflictException("Please pause before submitting more content");
        Boolean duplicate=db.queryForObject("SELECT EXISTS(SELECT 1 FROM "+table+" WHERE author_id=? AND body=? AND deleted_at IS NULL AND (?::uuid IS NULL OR id<>?) AND created_at>CURRENT_TIMESTAMP-INTERVAL '10 minutes')",Boolean.class,owner,body.trim(),excludedId,excludedId);
        if(Boolean.TRUE.equals(duplicate)) throw new ConflictException("This submission was already received; check your content before retrying");
        try { return safety.assess(body); }
        catch (IllegalArgumentException invalid) { throw invalid; }
        catch (RuntimeException unavailable) { return new ContentSafety.Assessment(ContentSafety.Outcome.HOLD_FOR_REVIEW,"SCREEN_UNAVAILABLE"); }
    }
    public void record(UUID id, boolean comment, long revision, ContentSafety.Assessment result) {
        db.update("INSERT INTO social_assessments("+(comment?"comment_id":"post_id")+",revision,policy_version,outcome,reason_code,initial_outcome,initial_reason_code) VALUES (?,?,?,?,?,?,?)",
                id,revision,ContentSafety.VERSION,result.outcome().name(),result.reason(),result.outcome().name(),result.reason());
    }
}
