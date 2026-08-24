package com.greenocean.backend.community.service;

import com.greenocean.backend.common.api.PageResponse;
import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.common.exception.ForbiddenException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.community.dto.CommunityResponse;
import com.greenocean.backend.community.dto.CreateCommunityRequest;
import com.greenocean.backend.community.repository.CommunityRepository;
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

    @Transactional
    public CommunityResponse join(UUID communityId, UUID userId) {
        CommunityResponse community = getById(communityId, userId);
        if (community.privateCommunity() && !community.member()) {
            throw new ForbiddenException("Private communities require an invitation");
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

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
