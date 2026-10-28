package com.greenocean.backend.post.service;

import com.greenocean.backend.auth.entity.User;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.common.api.PageResponse;
import com.greenocean.backend.common.exception.ForbiddenException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.post.dto.CommentResponse;
import com.greenocean.backend.post.dto.CreateCommentRequest;
import com.greenocean.backend.post.dto.CreatePostRequest;
import com.greenocean.backend.post.dto.PostResponse;
import com.greenocean.backend.post.entity.Comment;
import com.greenocean.backend.post.entity.ContentStatus;
import com.greenocean.backend.post.entity.Post;
import com.greenocean.backend.post.entity.PostVisibility;
import com.greenocean.backend.post.repository.CommentRepository;
import com.greenocean.backend.post.repository.PostReadRepository;
import com.greenocean.backend.post.repository.PostRepository;
import com.greenocean.backend.notification.service.NotificationService;
import com.greenocean.backend.social.service.SocialInteractionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PostService {
    private final SocialPublishing publishing;
    private final com.greenocean.backend.community.service.GroupPolicy groups;
    private final org.springframework.jdbc.core.JdbcTemplate db;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostReadRepository postReadRepository;
    private final UserRepository userRepository;
    private final SocialInteractionRepository interactions;
    private final DatabaseUuidGenerator uuidGenerator;
    private final NotificationService notificationService;

    public PostService(PostRepository postRepository, CommentRepository commentRepository,
                       PostReadRepository postReadRepository, UserRepository userRepository,
                       SocialInteractionRepository interactions, DatabaseUuidGenerator uuidGenerator,
                       NotificationService notificationService, SocialPublishing publishing, org.springframework.jdbc.core.JdbcTemplate db,com.greenocean.backend.community.service.GroupPolicy groups) {
        this.publishing=publishing;this.db=db;this.groups=groups;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postReadRepository = postReadRepository;
        this.userRepository = userRepository;
        this.interactions = interactions;
        this.uuidGenerator = uuidGenerator;
        this.notificationService = notificationService;
    }

    @Transactional
    public PostResponse create(UUID userId, CreatePostRequest request) {
        if (request.visibility() == PostVisibility.PUBLIC && !Boolean.TRUE.equals(request.broaderAudienceConfirmed()))
            throw new IllegalArgumentException("Confirm the broader eligible-member audience before publishing");
        validateVisibility(userId, request.visibility(), request.communityId());
        String identity=request.publicationIdentity();
        if(identity==null && request.anonymous())identity="ANONYMOUS"; // safe legacy anonymous intent only
        if(identity==null)throw new IllegalArgumentException("Choose an explicit publication identity; upgrade this client before named publication");
        boolean anonymous="ANONYMOUS".equals(identity);
        if(request.anonymous()!=anonymous)throw new IllegalArgumentException("Publication identity does not match anonymous intent");
        if(request.categoryId()!=null && !Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM support_categories WHERE id=?)",Boolean.class,request.categoryId())))throw new IllegalArgumentException("Choose a valid optional topic");
        var assessment=publishing.check(userId,"POST",request.body(),null);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User was not found"));
        Post post = new Post(
                uuidGenerator.nextUuid(), author, request.communityId(), request.body().trim(), anonymous,
                request.visibility(), normalizeOptional(request.contentWarning()), request.categoryId(), request.postType(),
                normalizeOptional(request.mood())
        );
        post.assess(assessment);
        postRepository.saveAndFlush(post);
        publishing.record(post.getId(),false,post.getRevision(),assessment);
        return postReadRepository.findOwnedPost(post.getId(),userId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> feed(UUID userId, int page, int size) {
        List<PostResponse> results = postReadRepository.findFeed(userId, page, size);
        return page(results, page, size);
    }

    @Transactional(readOnly=true)
    public com.greenocean.backend.post.dto.SocialFeedResponse discovery(UUID user,int size,String mode,UUID category,String cursor) {
        if(!java.util.Set.of("FOLLOWING","DISCOVER","SAVED").contains(mode))throw new IllegalArgumentException("Unsupported feed mode");
        java.time.Instant before=null;UUID beforeId=null;
        if(cursor!=null){try{String[] parts=cursor.split("\\|",-1);if(parts.length!=2)throw new IllegalArgumentException();before=java.time.Instant.parse(parts[0]);beforeId=UUID.fromString(parts[1]);}catch(RuntimeException bad){throw new IllegalArgumentException("Invalid feed cursor");}}
        var results=postReadRepository.findDiscovery(user,size,mode,category,before,beforeId);boolean more=results.size()>size;
        var items=List.copyOf(results.subList(0,Math.min(size,results.size())));
        String next=more?items.getLast().createdAt()+"|"+items.getLast().id():null;
        return new com.greenocean.backend.post.dto.SocialFeedResponse(items,more,next,
                mode.equals("FOLLOWING") ? postReadRepository.followingAnyone(user) : null);
    }
    @Transactional(readOnly = true)
    public PostResponse get(UUID postId, UUID userId) {
        return postReadRepository.findAccessiblePost(postId, userId)
                .orElseThrow(() -> new NotFoundException("Post was not found"));
    }

    @Transactional
    public void delete(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post was not found"));
        if (!post.getAuthorId().equals(userId)) throw new ForbiddenException("Only the author can delete this post");
        if (post.getStatus() != ContentStatus.DELETED) post.softDelete();
        postRepository.flush();
    }

    @Transactional
    public CommentResponse comment(UUID postId, UUID userId, CreateCommentRequest request) {
        get(postId, userId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post was not found"));
        checkGroup(post.getCommunityId(),userId);
        var assessment=publishing.check(userId,"COMMENT",request.body(),null);
        if (request.parentCommentId() != null
                && !Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM comments c WHERE c.id=? AND c.post_id=? AND c.status='PUBLISHED' AND NOT EXISTS(SELECT 1 FROM blocks b WHERE (b.blocker_id=? AND b.blocked_id=c.author_id) OR (b.blocker_id=c.author_id AND b.blocked_id=?)))",Boolean.class,request.parentCommentId(),postId,userId,userId))) {
            throw new IllegalArgumentException("Parent comment does not belong to this post");
        }
        if(request.parentCommentId()!=null) {
            Long depth=db.queryForObject("WITH RECURSIVE parents AS (SELECT id,parent_comment_id,1 AS depth FROM comments WHERE id=? UNION ALL SELECT c.id,c.parent_comment_id,p.depth+1 FROM comments c JOIN parents p ON c.id=p.parent_comment_id WHERE p.depth<8) SELECT MAX(depth) FROM parents",Long.class,request.parentCommentId());
            if(depth!=null && depth>=8)throw new IllegalArgumentException("Reply depth reached; start another response to the post");
        }
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User was not found"));
        Comment comment = new Comment(
                uuidGenerator.nextUuid(), post, author, request.parentCommentId(), request.body().trim(), request.anonymous() || Boolean.TRUE.equals(db.queryForObject("SELECT is_anonymous AND author_id=? FROM posts WHERE id=?",Boolean.class,userId,postId))
        );
        comment.assess(assessment);
        commentRepository.saveAndFlush(comment);
        publishing.record(comment.getId(),true,comment.getRevision(),assessment);
        if(assessment.outcome()==ContentSafety.Outcome.ALLOW)notificationService.commentCreated(userId,
            Boolean.TRUE.equals(db.queryForObject("SELECT is_anonymous FROM comments WHERE id=?",Boolean.class,comment.getId())), postId, comment.getId(), request.parentCommentId());
        return postReadRepository.findOwnedComment(comment.getId(), userId)
                .orElseThrow(() -> new IllegalStateException("Created comment could not be read"));
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> comments(UUID postId, UUID userId, int page, int size) {
        get(postId, userId);
        List<CommentResponse> results = postReadRepository.findComments(postId, userId, page, size);
        return page(results, page, size);
    }

    @Transactional
    public void setPostLike(UUID postId, UUID userId, boolean liked) {
        get(postId, userId);
        if (liked) {
            if (interactions.likePost(userId, postId)) notificationService.postLiked(userId, postId);
        } else {
            interactions.unlikePost(userId, postId);
        }
    }

    @Transactional
    public void setBookmark(UUID postId, UUID userId, boolean bookmarked) {
        if (!bookmarked) {interactions.removeBookmark(userId, postId);return;}
        get(postId, userId);interactions.bookmark(userId,postId);
    }

    @Transactional
    public void setCommentLike(UUID commentId, UUID userId, boolean liked) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment was not found"));
        get(comment.getPostId(), userId);
        // Post access does not grant permission to interact with every comment author.
        postReadRepository.findComment(commentId,userId)
                .orElseThrow(() -> new NotFoundException("Comment was not found"));
        if (liked) {
            if (interactions.likeComment(userId, commentId)) notificationService.commentLiked(userId, commentId);
        } else {
            interactions.unlikeComment(userId, commentId);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> communityFeed(UUID communityId, UUID userId, int page, int size) {
        groups.member(communityId,userId);
        return page(postReadRepository.findCommunityPosts(communityId, userId, page, size), page, size);
    }

    @Transactional(readOnly=true)
    public PageResponse<PostResponse> owned(UUID owner,int page,int size) {return page(postReadRepository.findOwned(owner,page,size),page,size);}
    @Transactional
    public PostResponse edit(UUID id,UUID owner,com.greenocean.backend.post.dto.EditPostRequest request) {
        db.queryForList("SELECT id FROM posts WHERE id=? FOR UPDATE",id);
        Post post=postRepository.findById(id).orElseThrow(()->new NotFoundException("Post was not found"));
        if(!post.getAuthorId().equals(owner) || post.getStatus()==ContentStatus.DELETED)throw new NotFoundException("Post was not found");
        if(post.getRevision()!=request.revision())throw new com.greenocean.backend.common.exception.ConflictException("Post changed; reload before editing");
        checkGroup(post.getCommunityId(),owner);
        var assessment=publishing.check(owner,"POST",request.body(),id);
        post.revise(request.body().trim());post.assess(assessment);postRepository.flush();
        publishing.record(id,false,post.getRevision(),assessment);
        return postReadRepository.findOwnedPost(id,owner).orElseThrow();
    }
    @Transactional
    public void deleteComment(UUID id,UUID owner) {
        Comment comment=commentRepository.findById(id).orElseThrow(()->new NotFoundException("Comment was not found"));
        if(!comment.getAuthorId().equals(owner))throw new NotFoundException("Comment was not found");
        comment.softDelete();commentRepository.flush();
    }
    private void checkGroup(UUID group,UUID user){if(group!=null){groups.lock(group);groups.contribution(group,user);}}
    private void validateVisibility(UUID userId, PostVisibility visibility, UUID communityId) {
        if (visibility == PostVisibility.COMMUNITY) {
            if (communityId == null) throw new IllegalArgumentException("communityId is required for COMMUNITY visibility");
            checkGroup(communityId,userId);
            if (!interactions.isCommunityMember(communityId, userId)) {
                throw new ForbiddenException("You must be a member of the community to publish there");
            }
        } else if (communityId != null) {
            throw new IllegalArgumentException("communityId is only allowed for COMMUNITY visibility");
        }
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private <T> PageResponse<T> page(List<T> results, int page, int size) {
        boolean hasNext = results.size() > size;
        List<T> items = hasNext ? List.copyOf(results.subList(0, size)) : List.copyOf(results);
        return new PageResponse<>(items, page, size, hasNext);
    }
}
