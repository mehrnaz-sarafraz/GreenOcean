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
@Table(name = "posts")
public class Post {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "community_id")
    private UUID communityId;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentStatus status;

    @Column(name = "content_warning", length = 120)
    private String contentWarning;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Post() {
    }

    public Post(UUID id, User author, UUID communityId, String body, boolean anonymous,
                PostVisibility visibility, String contentWarning) {
        Instant now = Instant.now();
        this.id = id;
        this.author = author;
        this.communityId = communityId;
        this.body = body;
        this.anonymous = anonymous;
        this.visibility = visibility;
        this.status = ContentStatus.PUBLISHED;
        this.contentWarning = contentWarning;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void softDelete() {
        status = ContentStatus.DELETED;
        deletedAt = Instant.now();
        updatedAt = deletedAt;
    }

    public UUID getId() { return id; }
    public UUID getAuthorId() { return author.getId(); }
    public UUID getCommunityId() { return communityId; }
    public PostVisibility getVisibility() { return visibility; }
    public ContentStatus getStatus() { return status; }
}
