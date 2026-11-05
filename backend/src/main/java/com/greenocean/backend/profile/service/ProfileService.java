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
import com.greenocean.backend.social.service.SocialInteractionRepository;
import com.greenocean.backend.post.repository.PostReadRepository;
import com.greenocean.backend.post.dto.PostResponse;
import com.greenocean.backend.common.api.PageResponse;
import com.greenocean.backend.common.exception.ForbiddenException;
import java.util.List;

@Service
public class ProfileService {
    private final com.greenocean.backend.auth.repository.UserRepository users;
    private final com.greenocean.backend.identity.AccessPolicy access;
    private final ProfileRepository profileRepository;
    private final SocialInteractionRepository interactions;
    private final PostReadRepository postReadRepository;

    public ProfileService(ProfileRepository profileRepository, SocialInteractionRepository interactions,
                          PostReadRepository postReadRepository, com.greenocean.backend.auth.repository.UserRepository users, com.greenocean.backend.identity.AccessPolicy access) {
        this.users=users; this.access=access;
        this.profileRepository = profileRepository;
        this.interactions = interactions;
        this.postReadRepository = postReadRepository;
    }

    @Transactional(readOnly = true)
    public OwnProfileResponse getOwnProfile(UUID userId) {
        return toOwnResponse(findByUserId(userId));
    }

    @Transactional
    public OwnProfileResponse updateOwnProfile(UUID userId, UpdateProfileRequest request) {
        Profile profile = profileRepository.findForUpdate(userId).orElseThrow(() -> new NotFoundException("Profile was not found"));
        if(request.countryCode()!=null && !request.countryCode().isBlank()) access.country(request.countryCode().toUpperCase(java.util.Locale.ROOT));
        String username = normalizeUsername(request.username());
        if (username != null && username.matches("(admin|administrator|support|greenocean|moderator|system|official|staff)([._].*)?"))
            throw new IllegalArgumentException("This username is reserved");
        validateAvatar(request.avatarUrl());
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
    public PublicProfileResponse getPublicProfile(UUID viewerId, String username) {
        Profile profile = profileRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new NotFoundException("Profile was not found"));
        UUID targetId = profile.getUserId();
        if(users.findById(targetId).map(u -> u.getStatus()!=com.greenocean.backend.auth.entity.UserStatus.ACTIVE).orElse(true)) throw new NotFoundException("Profile was not found");
        if (interactions.hasBlockBetween(viewerId, targetId)) {
            throw new NotFoundException("Profile was not found");
        }
        boolean ownProfile = viewerId.equals(targetId);
        boolean following = ownProfile || interactions.isFollowing(viewerId, targetId);
        boolean canSeeActivity = ownProfile || !profile.isProfilePrivate() || following;
        return new PublicProfileResponse(
                targetId,
                profile.getUsername(),
                profile.getDisplayName(),
                canSeeActivity ? profile.getBio() : null,
                profile.getAvatarUrl(),
                profile.isShowLocation() && canSeeActivity ? profile.getCountryCode() : null,
                profile.isShowLocation() && canSeeActivity ? profile.getCity() : null,
                profile.isShowBirthYear() && canSeeActivity ? profile.getBirthYear() : null,
                profile.isProfilePrivate(),
                following && !ownProfile,
                !following && interactions.hasPendingFollowRequest(viewerId, targetId),
                canSeeActivity,
                canSeeActivity ? interactions.followerCount(targetId) : 0,
                canSeeActivity ? interactions.followingCount(targetId) : 0,
                interactions.visiblePostCount(targetId, viewerId),
                0, // Historical reaction totals can identify anonymous/private activity.
                profile.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> getPublicPosts(UUID viewerId, String username, int page, int size) {
        Profile profile = profileRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new NotFoundException("Profile was not found"));
        if(users.findById(profile.getUserId()).map(u -> u.getStatus()!=com.greenocean.backend.auth.entity.UserStatus.ACTIVE).orElse(true)) throw new NotFoundException("Profile was not found");
        if (interactions.hasBlockBetween(viewerId, profile.getUserId())) {
            throw new NotFoundException("Profile was not found");
        }
        if (profile.isProfilePrivate() && !viewerId.equals(profile.getUserId())
                && !interactions.isFollowing(viewerId, profile.getUserId())) {
            throw new ForbiddenException("Follow this private profile to view its stories");
        }
        List<PostResponse> results = postReadRepository.findAuthorPosts(profile.getUserId(), viewerId, page, size);
        boolean hasNext = results.size() > size;
        return new PageResponse<>(hasNext ? List.copyOf(results.subList(0, size)) : List.copyOf(results),
                page, size, hasNext);
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
        String trimmed = java.text.Normalizer.normalize(value.trim(), java.text.Normalizer.Form.NFC);
        if (trimmed.isEmpty()) throw new IllegalArgumentException(fieldName + " cannot be blank");
        if (trimmed.codePoints().anyMatch(c -> Character.isISOControl(c) || (c >= 0x202a && c <= 0x202e) || (c >= 0x2066 && c <= 0x2069)))
            throw new IllegalArgumentException(fieldName + " contains unsupported control characters");
        return trimmed;
    }

    private void validateAvatar(String value) {
        if (value == null || value.isBlank()) return;
        try {
            var uri = java.net.URI.create(value.trim());
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null || uri.getUserInfo() != null ||
                !host.contains(".") || host.matches("[0-9.]+") || host.contains(":") ||
                host.endsWith(".localhost") || host.endsWith(".local") || uri.getFragment() != null)
                throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Avatar must be a public HTTPS image URL without credentials, or empty to remove");
        }
    }
}
