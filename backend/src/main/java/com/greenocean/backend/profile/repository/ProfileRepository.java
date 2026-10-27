package com.greenocean.backend.profile.repository;

import com.greenocean.backend.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Profile p where p.userId = :userId")
    Optional<Profile> findForUpdate(@org.springframework.data.repository.query.Param("userId") UUID userId);
    boolean existsByUsernameIgnoreCase(String username);

    Optional<Profile> findByUsernameIgnoreCase(String username);
}
