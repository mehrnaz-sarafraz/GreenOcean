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
import com.greenocean.backend.post.entity.FeedMode;
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
                       NotificationService notificationService) {
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
        validateVisibility(userId, request.visibility(), request.communityId());
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User was not found"));
        Post post = new Post(
                uuidGenerator.nextUuid(), author, request.communityId(), request.body().trim(), request.anonymous(),
                request.visibility(), normalizeOptional(request.contentWarning()), request.categoryId(), request.postType(),
                normalizeOptional(request.mood())
        );
        postRepository.saveAndFlush(post);
        return get(post.getId(), userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> feed(UUID userId, FeedMode mode, int page, int size) {
        List<PostResponse> results = postReadRepository.findFeed(userId, mode, page, size);
        return page(results, page, size);
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
    }

    @Transactional
    public CommentResponse comment(UUID postId, UUID userId, CreateCommentRequest request) {
        get(postId, userId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post was not found"));
        if (request.parentCommentId() != null
                && !commentRepository.existsByIdAndPost_Id(request.parentCommentId(), postId)) {
            throw new IllegalArgumentException("Parent comment does not belong to this post");
        }
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User was not found"));
        Comment comment = new Comment(
                uuidGenerator.nextUuid(), post, author, request.parentCommentId(), request.body().trim(), request.anonymous()
        );
        commentRepository.saveAndFlush(comment);
        notificationService.commentCreated(userId, request.anonymous(), postId, comment.getId(), request.parentCommentId());
        return postReadRepository.findComment(comment.getId(), userId)
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
        get(postId, userId);
        if (bookmarked) interactions.bookmark(userId, postId); else interactions.removeBookmark(userId, postId);
    }

    @Transactional
    public void setCommentLike(UUID commentId, UUID userId, boolean liked) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment was not found"));
        get(comment.getPostId(), userId);
        if (liked) {
            if (interactions.likeComment(userId, commentId)) notificationService.commentLiked(userId, commentId);
        } else {
            interactions.unlikeComment(userId, commentId);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> communityFeed(UUID communityId, UUID userId, int page, int size) {
        if (!interactions.isCommunityMember(communityId, userId)) {
            throw new ForbiddenException("You must be a member to view community posts");
        }
        return page(postReadRepository.findCommunityPosts(communityId, userId, page, size), page, size);
    }

    private void validateVisibility(UUID userId, PostVisibility visibility, UUID communityId) {
        if (visibility == PostVisibility.COMMUNITY) {
            if (communityId == null) throw new IllegalArgumentException("communityId is required for COMMUNITY visibility");
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
