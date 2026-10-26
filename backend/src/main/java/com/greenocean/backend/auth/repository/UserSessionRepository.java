package com.greenocean.backend.auth.repository;

import com.greenocean.backend.auth.entity.UserSession;
import com.greenocean.backend.auth.entity.UserStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.Instant;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    @Query("select s from UserSession s join fetch s.user u where s.refreshTokenHash = :refreshTokenHash")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update UserSession session
               set session.revokedAt = :revokedAt
             where session.user.id = :userId
               and session.revokedAt is null
            """)
    int revokeAllActiveByUserId(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);

    @Query("""
            select session from UserSession session
             where session.user.id = :userId
               and session.revokedAt is null
               and session.expiresAt > :now
             order by coalesce(session.lastUsedAt, session.createdAt) desc
            """)
    List<UserSession> findActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    @Query("""
            select session from UserSession session
             where session.id = :sessionId and session.user.id = :userId
            """)
    Optional<UserSession> findOwned(@Param("sessionId") UUID sessionId, @Param("userId") UUID userId);

    @Query("""
            select (count(session) > 0) from UserSession session
             where session.id = :sessionId
               and session.user.id = :userId
               and session.revokedAt is null
               and session.expiresAt > :now
               and (session.user.status = :userStatus or session.user.status = com.greenocean.backend.auth.entity.UserStatus.PENDING)
            """)
    boolean isActive(@Param("sessionId") UUID sessionId,
                     @Param("userId") UUID userId,
                     @Param("now") Instant now,
                     @Param("userStatus") UserStatus userStatus);
}
