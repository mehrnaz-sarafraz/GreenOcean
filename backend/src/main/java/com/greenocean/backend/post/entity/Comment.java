package com.greenocean.backend.post.entity;

import com.greenocean.backend.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "parent_comment_id")
    private UUID parentCommentId;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(nullable=false) private long revision = 1;
    @Column(name="safety_outcome") private String safetyOutcome;
    @Column(name="safety_reason") private String safetyReason;
    public long getRevision() { return revision; }
    public void assess(com.greenocean.backend.post.service.ContentSafety.Assessment assessment) {
        safetyOutcome=assessment.outcome().name(); safetyReason=assessment.reason();
        status=assessment.outcome()==com.greenocean.backend.post.service.ContentSafety.Outcome.ALLOW ? ContentStatus.PUBLISHED : ContentStatus.UNDER_REVIEW;
    }
    public void revise(String text) { body=text; revision++; updatedAt=Instant.now(); }
    protected Comment() {
    }

    public Comment(UUID id, Post post, User author, UUID parentCommentId, String body, boolean anonymous) {
        Instant now = Instant.now();
        this.id = id;
        this.post = post;
        this.author = author;
        this.parentCommentId = parentCommentId;
        this.body = body;
        this.anonymous = anonymous;
        this.status = ContentStatus.PUBLISHED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getAuthorId() {return author.getId();}
    public ContentStatus getStatus() {return status;}
    public void softDelete() {status=ContentStatus.DELETED;deletedAt=Instant.now();updatedAt=deletedAt;}
    public UUID getId() { return id; }
    public UUID getPostId() { return post.getId(); }
}
