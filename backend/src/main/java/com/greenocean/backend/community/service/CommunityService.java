package com.greenocean.backend.community.service;

import com.greenocean.backend.common.api.PageResponse;
import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.common.exception.ForbiddenException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.community.dto.CommunityResponse;
import com.greenocean.backend.community.dto.CommunityDetailsResponse;
import com.greenocean.backend.community.dto.CreateCommunityRequest;
import com.greenocean.backend.community.repository.CommunityRepository;
import com.greenocean.backend.community.dto.CommunityJoinRequestResponse;
import com.greenocean.backend.community.dto.CreateCommunityEventRequest;
import com.greenocean.backend.community.dto.UpdateCommunityRulesRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final DatabaseUuidGenerator uuidGenerator;

    public CommunityService(CommunityRepository communityRepository, DatabaseUuidGenerator uuidGenerator) {
        this.communityRepository = communityRepository;
        this.uuidGenerator = uuidGenerator;
    }

    @Transactional
    public CommunityResponse create(UUID userId, CreateCommunityRequest request) {
        UUID id = uuidGenerator.nextUuid();
        try {
            communityRepository.create(id, userId, request.name().trim(), request.slug().trim(),
                    normalize(request.description()), request.privateCommunity());
        } catch (IllegalStateException exception) {
            throw new ConflictException(exception.getMessage());
        }
        return getById(id, userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunityResponse> discover(UUID userId, String query, int page, int size) {
        List<CommunityResponse> results = communityRepository.findAll(userId, query == null ? "" : query, page, size);
        boolean hasNext = results.size() > size;
        List<CommunityResponse> items = hasNext ? List.copyOf(results.subList(0, size)) : List.copyOf(results);
        return new PageResponse<>(items, page, size, hasNext);
    }

    @Transactional(readOnly = true)
    public CommunityResponse getBySlug(String slug, UUID userId) {
        return communityRepository.findBySlug(slug, userId)
                .orElseThrow(() -> new NotFoundException("Community was not found"));
    }

    @Transactional(readOnly = true)
    public CommunityResponse getById(UUID communityId, UUID userId) {
        return communityRepository.findById(communityId, userId)
                .orElseThrow(() -> new NotFoundException("Community was not found"));
    }

    @Transactional(readOnly = true)
    public CommunityDetailsResponse details(UUID communityId, UUID userId) {
        getById(communityId, userId);
        requireMember(communityId, userId);
        return communityRepository.details(communityId, userId);
    }

    @Transactional
    public CommunityDetailsResponse joinEvent(UUID communityId, UUID eventId, UUID userId) {
        getById(communityId, userId);
        requireMember(communityId, userId);
        if (!communityRepository.eventBelongsToCommunity(eventId, communityId)) {
            throw new NotFoundException("Community event was not found");
        }
        int inserted = communityRepository.joinEvent(eventId, userId);
        if (inserted == 0 && !communityRepository.isEventAttendee(eventId, userId)) {
            throw new ConflictException("This event has reached its capacity");
        }
        return communityRepository.details(communityId, userId);
    }

    @Transactional
    public CommunityDetailsResponse leaveEvent(UUID communityId, UUID eventId, UUID userId) {
        getById(communityId, userId);
        requireMember(communityId, userId);
        if (!communityRepository.eventBelongsToCommunity(eventId, communityId)) {
            throw new NotFoundException("Community event was not found");
        }
        communityRepository.leaveEvent(eventId, userId);
        return communityRepository.details(communityId, userId);
    }

    @Transactional
    public CommunityResponse join(UUID communityId, UUID userId) {
        CommunityResponse community = getById(communityId, userId);
        if (community.privateCommunity() && !community.member()) {
            communityRepository.requestJoin(communityId, userId);
            return getById(communityId, userId);
        }
        communityRepository.join(communityId, userId);
        return getById(communityId, userId);
    }

    @Transactional
    public void leave(UUID communityId, UUID userId) {
        getById(communityId, userId);
        String role = communityRepository.membershipRole(communityId, userId);
        if (role == null) return;
        if ("OWNER".equals(role)) throw new ConflictException("The owner cannot leave the community");
        communityRepository.leave(communityId, userId);
    }

    @Transactional
    public void cancelJoinRequest(UUID communityId, UUID userId) {
        getById(communityId, userId);
        communityRepository.cancelJoinRequest(communityId, userId);
    }

    @Transactional(readOnly = true)
    public List<CommunityJoinRequestResponse> joinRequests(UUID communityId, UUID userId) {
        requireModerator(communityId, userId);
        return communityRepository.joinRequests(communityId);
    }

    @Transactional
    public void resolveJoinRequest(UUID communityId, UUID requesterId, UUID userId, boolean approved) {
        requireModerator(communityId, userId);
        if (!communityRepository.resolveJoinRequest(communityId, requesterId, userId, approved)) {
            throw new NotFoundException("Pending community request was not found");
        }
    }

    @Transactional
    public CommunityDetailsResponse replaceRules(UUID communityId, UUID userId, UpdateCommunityRulesRequest request) {
        requireModerator(communityId, userId);
        List<String> rules = request.rules().stream().map(String::trim).filter(rule -> !rule.isBlank()).distinct().toList();
        if (rules.isEmpty()) throw new IllegalArgumentException("At least one rule is required");
        communityRepository.replaceRules(communityId, rules);
        return communityRepository.details(communityId, userId);
    }

    @Transactional
    public CommunityDetailsResponse createEvent(UUID communityId, UUID userId, CreateCommunityEventRequest request) {
        requireModerator(communityId, userId);
        if (!request.endsAt().isAfter(request.startsAt())) throw new IllegalArgumentException("endsAt must be after startsAt");
        String format = request.format().trim().toUpperCase();
        if (!List.of("TEXT_CIRCLE", "VIDEO", "AUDIO", "WORKSHOP", "Q_AND_A").contains(format)) {
            throw new IllegalArgumentException("Unsupported event format");
        }
        communityRepository.createEvent(uuidGenerator.nextUuid(), communityId, userId, request.title().trim(),
                normalize(request.description()), format, request.startsAt(), request.endsAt(), request.capacity());
        return communityRepository.details(communityId, userId);
    }

    @Transactional
    public CommunityDetailsResponse cancelEvent(UUID communityId, UUID eventId, UUID userId) {
        requireModerator(communityId, userId);
        if (!communityRepository.cancelEvent(communityId, eventId)) throw new NotFoundException("Active event was not found");
        return communityRepository.details(communityId, userId);
    }

    @Transactional
    public CommunityDetailsResponse updateMemberRole(UUID communityId, UUID memberId, UUID userId, String role) {
        requireOwner(communityId, userId);
        if (!List.of("MEMBER", "MODERATOR").contains(role)) throw new IllegalArgumentException("Unsupported community role");
        if (!communityRepository.updateMemberRole(communityId, memberId, role)) throw new NotFoundException("Community member was not found");
        return communityRepository.details(communityId, userId);
    }

    @Transactional
    public CommunityDetailsResponse removeMember(UUID communityId, UUID memberId, UUID userId) {
        String currentRole = communityRepository.membershipRole(communityId, userId);
        if (!"OWNER".equals(currentRole) && !"MODERATOR".equals(currentRole)) throw new ForbiddenException("Community moderators only");
        String targetRole = communityRepository.membershipRole(communityId, memberId);
        if ("OWNER".equals(targetRole) || ("MODERATOR".equals(targetRole) && !"OWNER".equals(currentRole))) {
            throw new ForbiddenException("Only the owner can remove a moderator");
        }
        if (!communityRepository.removeMember(communityId, memberId)) throw new NotFoundException("Community member was not found");
        return communityRepository.details(communityId, userId);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void requireMember(UUID communityId, UUID userId) {
        if (!communityRepository.isMember(communityId, userId)) {
            throw new ForbiddenException("Join this community to view its members and events");
        }
    }

    private void requireModerator(UUID communityId, UUID userId) {
        String role = communityRepository.membershipRole(communityId, userId);
        if (!"OWNER".equals(role) && !"MODERATOR".equals(role)) throw new ForbiddenException("Community moderators only");
    }

    private void requireOwner(UUID communityId, UUID userId) {
        if (!"OWNER".equals(communityRepository.membershipRole(communityId, userId))) {
            throw new ForbiddenException("Community owner only");
        }
    }
}
