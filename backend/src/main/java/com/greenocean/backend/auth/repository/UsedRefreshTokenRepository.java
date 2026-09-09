package com.greenocean.backend.auth.repository;

import com.greenocean.backend.auth.entity.UsedRefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface UsedRefreshTokenRepository extends JpaRepository<UsedRefreshToken, String> {
    @EntityGraph(attributePaths = {"session", "session.user"})
    Optional<UsedRefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from UsedRefreshToken token where token.expiresAt <= :now")
    int deleteExpired(@Param("now") Instant now);
}
