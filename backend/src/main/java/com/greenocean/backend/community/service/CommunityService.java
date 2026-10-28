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
    private final GroupActivities activities;private final GroupPolicy policy;private final GroupMemberships memberships;private final com.greenocean.backend.post.service.ContentClassifier safety;

    public CommunityService(CommunityRepository communityRepository, DatabaseUuidGenerator uuidGenerator,GroupPolicy policy,GroupMemberships memberships,GroupActivities activities,com.greenocean.backend.post.service.ContentClassifier safety) {
        this.communityRepository = communityRepository;
        this.uuidGenerator = uuidGenerator;
        this.activities=activities;this.policy=policy;this.memberships=memberships;this.safety=safety;
    }

    @Transactional
    public CommunityResponse create(UUID userId, CreateCommunityRequest request) {
        throw new ConflictException("Submit a group proposal for platform review; immediate creation is unavailable");
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
        activities.attendance(communityId,eventId,userId,true);
        return communityRepository.details(communityId, userId);
    }

    @Transactional
    public CommunityDetailsResponse leaveEvent(UUID communityId, UUID eventId, UUID userId) {
        activities.attendance(communityId,eventId,userId,false);
        return communityRepository.details(communityId, userId);
    }

    @Transactional
    public CommunityResponse join(UUID communityId, UUID userId,long rulesVersion) {
        memberships.join(communityId,userId,rulesVersion);
        return getById(communityId, userId);
    }

    @Transactional
    public void leave(UUID communityId, UUID userId) {
        memberships.leave(communityId,userId);
    }

    @Transactional
    public void cancelJoinRequest(UUID communityId, UUID userId,long revision) {
        memberships.cancel(communityId,userId,revision);
    }

    @Transactional(readOnly = true)
    public List<CommunityJoinRequestResponse> joinRequests(UUID communityId, UUID userId) {
        requireModerator(communityId, userId);
        return communityRepository.joinRequests(communityId);
    }

    @Transactional
    public void resolveJoinRequest(UUID communityId, UUID requesterId, UUID userId, boolean approved,long revision) {
        memberships.resolve(communityId,requesterId,userId,revision,approved);
    }

    @Transactional
    public CommunityDetailsResponse replaceRules(UUID communityId, UUID userId, UpdateCommunityRulesRequest request) {
        policy.lock(communityId);policy.writable(communityId);
        requireModerator(communityId, userId);
        List<String> rules = request.rules().stream().map(String::trim).filter(rule -> !rule.isBlank()).distinct().toList();
        if (rules.isEmpty()) throw new IllegalArgumentException("At least one rule is required");
        if(safety.assess(String.join("\n",rules)).outcome()!=com.greenocean.backend.post.service.ContentSafety.Outcome.ALLOW)throw new ConflictException("Rules require revision under platform safety standards");
        policy.bound(userId,"GROUP_RULE_CHANGE",10);
        communityRepository.replaceRules(communityId, rules);
        policy.db.update("UPDATE communities SET rules_version=rules_version+1 WHERE id=?",communityId);
        policy.db.update("INSERT INTO community_rule_revisions(community_id,version,rules,actor_id) SELECT c.id,c.rules_version,(SELECT jsonb_agg(rule_text ORDER BY position) FROM community_rules WHERE community_id=c.id),? FROM communities c WHERE c.id=?",userId,communityId);
        policy.db.queryForList("SELECT user_id FROM community_members WHERE community_id=? LIMIT 1000",UUID.class,communityId).forEach(policy::notify);policy.audit(userId,communityId,"GROUP_RULES_CHANGED");
        return communityRepository.details(communityId, userId);
    }

    @Transactional
    public CommunityDetailsResponse createEvent(UUID communityId, UUID userId, CreateCommunityEventRequest request) {
        activities.save(communityId,userId,null,0,request);
        return communityRepository.details(communityId, userId);
    }

    @Transactional
    public CommunityDetailsResponse cancelEvent(UUID communityId, UUID eventId, UUID userId) {
        policy.lock(communityId);policy.manager(communityId,userId);
        Long revision=policy.db.queryForList("SELECT revision FROM community_events WHERE community_id=? AND id=?",Long.class,communityId,eventId).stream().findFirst().orElseThrow(()->new NotFoundException("Community was not found"));
        activities.transition(communityId,eventId,userId,revision,"CANCELLED");
        return communityRepository.details(communityId, userId);
    }

    @Transactional
    public CommunityDetailsResponse updateMemberRole(UUID communityId, UUID memberId, UUID userId, String role) {
        policy.lock(communityId);policy.writable(communityId);memberships.allowed(communityId,memberId);
        requireOwner(communityId, userId);
        if (!List.of("MEMBER", "MODERATOR").contains(role)) throw new IllegalArgumentException("Unsupported community role");
        if (!communityRepository.updateMemberRole(communityId, memberId, role)) throw new NotFoundException("Community member was not found");
        policy.bound(userId,"GROUP_ROLE_CHANGE",20);policy.audit(userId,communityId,"GROUP_ROLE_CHANGED");policy.notify(memberId);
        return communityRepository.details(communityId, userId);
    }

    @Transactional
    public CommunityDetailsResponse removeMember(UUID communityId, UUID memberId, UUID userId) {
        memberships.remove(communityId,memberId,userId,"REMOVE","Removed by an authorized group manager",null);
        return communityRepository.details(communityId, userId);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void requireMember(UUID communityId, UUID userId) {
        policy.member(communityId,userId);
    }

    private void requireModerator(UUID communityId, UUID userId) {
        policy.manager(communityId,userId);
    }

    private void requireOwner(UUID communityId, UUID userId) {
        policy.owner(communityId,userId);
    }
}
