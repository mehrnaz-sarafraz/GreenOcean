package com.greenocean.backend.post.repository;

import com.greenocean.backend.post.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    boolean existsByIdAndPost_Id(UUID id, UUID postId);
}
