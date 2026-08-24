package com.greenocean.backend.profile.service;

import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.profile.dto.OwnProfileResponse;
import com.greenocean.backend.profile.dto.PublicProfileResponse;
import com.greenocean.backend.profile.dto.UpdateProfileRequest;
import com.greenocean.backend.profile.entity.Profile;
import com.greenocean.backend.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Year;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public OwnProfileResponse getOwnProfile(UUID userId) {
        return toOwnResponse(findByUserId(userId));
    }

    @Transactional
    public OwnProfileResponse updateOwnProfile(UUID userId, UpdateProfileRequest request) {
        Profile profile = findByUserId(userId);
        String username = normalizeUsername(request.username());
        if (username != null
                && !username.equalsIgnoreCase(profile.getUsername())
                && profileRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username is already taken");
        }
        if (request.birthYear() != null && request.birthYear() > Year.now().getValue()) {
            throw new IllegalArgumentException("birthYear cannot be in the future");
        }

        profile.update(
                username,
                normalizeRequiredText(request.displayName(), "displayName"),
                request.bio(),
                request.avatarUrl(),
                normalizeCountryCode(request.countryCode()),
                request.city(),
                request.birthYear(),
                request.profilePrivate(),
                request.showLocation(),
                request.showBirthYear()
        );
        try {
            profileRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Profile information conflicts with an existing account");
        }
        return toOwnResponse(profile);
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(String username) {
        Profile profile = profileRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new NotFoundException("Profile was not found"));
        return new PublicProfileResponse(
                profile.getUserId(),
                profile.getUsername(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.isShowLocation() ? profile.getCountryCode() : null,
                profile.isShowLocation() ? profile.getCity() : null,
                profile.isShowBirthYear() ? profile.getBirthYear() : null,
                profile.isProfilePrivate()
        );
    }

    private Profile findByUserId(UUID userId) {
        return profileRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Profile was not found"));
    }

    private OwnProfileResponse toOwnResponse(Profile profile) {
        return new OwnProfileResponse(
                profile.getUserId(), profile.getUsername(), profile.getDisplayName(),
                profile.getBio(), profile.getAvatarUrl(), profile.getCountryCode(), profile.getCity(),
                profile.getBirthYear(), profile.isProfilePrivate(), profile.isShowLocation(),
                profile.isShowBirthYear(), profile.getCreatedAt(), profile.getUpdatedAt()
        );
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeCountryCode(String countryCode) {
        return countryCode == null ? null : countryCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeRequiredText(String value, String fieldName) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException(fieldName + " cannot be blank");
        return trimmed;
    }
}
