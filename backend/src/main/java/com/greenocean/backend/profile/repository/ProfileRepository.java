package com.greenocean.backend.profile.repository;

import com.greenocean.backend.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    boolean existsByUsernameIgnoreCase(String username);

    Optional<Profile> findByUsernameIgnoreCase(String username);
}
