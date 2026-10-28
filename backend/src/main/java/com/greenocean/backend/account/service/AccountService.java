package com.greenocean.backend.account.service;

import com.greenocean.backend.account.dto.AccountArchiveResponse;
import com.greenocean.backend.account.repository.AccountRepository;
import com.greenocean.backend.moderation.service.ReportService;
import com.greenocean.backend.preference.service.UserPreferencesService;
import com.greenocean.backend.profile.service.ProfileService;
import com.greenocean.backend.social.service.SocialService;
import com.greenocean.backend.wellbeing.service.WellbeingService;
import com.greenocean.backend.search.service.SearchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.exception.ForbiddenException;
import com.greenocean.backend.professional.service.ProfessionalDocumentStorage;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AccountService {
    private final ProfileService profiles;
    private final com.greenocean.backend.preference.service.PrivacyGovernanceService governance;
    private final UserPreferencesService preferences;
    private final WellbeingService wellbeing;
    private final ReportService reports;
    private final SocialService social;
    private final AccountRepository repository;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final ProfessionalDocumentStorage documentStorage;
    private final SearchService search;
    private final org.springframework.jdbc.core.JdbcTemplate db;

    public AccountService(com.greenocean.backend.preference.service.PrivacyGovernanceService governance,ProfileService profiles, UserPreferencesService preferences, WellbeingService wellbeing,
                          ReportService reports, SocialService social, AccountRepository repository,
                          UserRepository users, PasswordEncoder passwordEncoder,
                          ProfessionalDocumentStorage documentStorage, SearchService search, org.springframework.jdbc.core.JdbcTemplate db) {
        this.db=db;this.governance=governance;
        this.profiles = profiles;
        this.preferences = preferences;
        this.wellbeing = wellbeing;
        this.reports = reports;
        this.social = social;
        this.repository = repository;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.documentStorage = documentStorage;
        this.search = search;
    }

    @Transactional(readOnly = true)
    public AccountArchiveResponse archive(UUID userId) {
        return new AccountArchiveResponse(Instant.now(), profiles.getOwnProfile(userId), preferences.get(userId),
                wellbeing.snapshot(userId), reports.mine(userId), social.following(userId), social.followers(userId),
                social.blocked(userId), search.history(userId), repository.activity(userId),governance.archive(userId),
                db.queryForList("SELECT id,payload::text,revision,updated_at,expires_at FROM social_drafts WHERE owner_id=? AND expires_at>CURRENT_TIMESTAMP ORDER BY updated_at",userId),
                db.queryForList("SELECT a.id,a.post_id,a.comment_id,a.revision,a.policy_version,a.outcome,a.reason_code,a.assessed_at,a.decision_note,a.reviewed_at,a.initial_outcome,a.initial_reason_code,d.outcome AS review_outcome,d.note AS review_note,d.created_at AS reviewed_history_at,ap.id AS appeal_id,ap.reason AS appeal_reason,ap.status AS appeal_status,ap.decision_note AS appeal_note FROM social_assessments a LEFT JOIN social_review_decisions d ON d.assessment_id=a.id LEFT JOIN moderation_appeals ap ON ap.assessment_id=a.id LEFT JOIN posts p ON p.id=a.post_id LEFT JOIN comments c ON c.id=a.comment_id WHERE p.author_id=? OR c.author_id=? ORDER BY a.assessed_at",userId,userId),
                db.queryForList("SELECT id,request_status,requester_id,request_target_id,updated_at FROM conversations WHERE service_kind='COMMUNITY' AND (requester_id=? OR request_target_id=?) ORDER BY updated_at,id",userId,userId),
                java.util.Map.of("groupContentActions",db.queryForList("SELECT a.id,a.community_id,a.revision,a.reason,a.status,ap.status AS appeal_status,ap.reason AS appeal_reason,ap.decision_note FROM community_content_actions a LEFT JOIN posts post ON post.id=a.post_id LEFT JOIN comments c ON c.id=a.comment_id LEFT JOIN moderation_appeals ap ON ap.group_action_id=a.id WHERE post.author_id=? OR c.author_id=?",userId,userId),"attendance",db.queryForList("SELECT a.event_id,a.joined_at FROM community_event_attendees a WHERE a.user_id=?",userId),"ruleChanges",db.queryForList("SELECT community_id,version,rules::text,created_at FROM community_rule_revisions WHERE actor_id=? ORDER BY created_at",userId),"memberActions",db.queryForList("SELECT id,community_id,action,reason,expires_at,created_at FROM community_member_actions WHERE user_id=?",userId),"invitations",db.queryForList("SELECT id,community_id,status,expires_at,created_at FROM community_invitations WHERE target_id=? OR inviter_id=? ORDER BY created_at,id",userId,userId),"transfers",db.queryForList("SELECT id,community_id,status,expires_at FROM community_ownership_transfers WHERE target_id=? OR from_owner=?",userId,userId),"bans",db.queryForList("SELECT community_id,reason,expires_at FROM community_bans WHERE user_id=?",userId),"proposals",db.queryForList("SELECT id,name,description,purpose,privacy_type,rules::text,revision,status,decision_note,community_id,created_at FROM community_proposals WHERE owner_id=? ORDER BY created_at,id",userId)),
                listenerPlanning(userId));
    }

    private java.util.Map<String,Object> listenerPlanning(UUID user){
        var data=new java.util.LinkedHashMap<String,Object>();
        data.put("calendarSettings",db.queryForList("SELECT timezone,paused,revision,updated_at FROM listener_calendar_settings WHERE listener_id=?",user));
        data.put("calendarRules",db.queryForList("SELECT id,timezone,weekday,starts_local,ends_local,valid_from,valid_until,overlap_policy,active,retired_at FROM listener_calendar_rules WHERE listener_id=?",user));
        data.put("calendarExceptions",db.queryForList("SELECT id,timezone,starts_at,ends_at,available,active,retired_at FROM listener_calendar_exceptions WHERE listener_id=?",user));
        data.put("staffedWindows",db.queryForList("SELECT id,starts_at,ends_at,revoked_at FROM listener_queue_coverage_windows WHERE listener_id=?",user));
        data.put("bookings",db.queryForList("SELECT id,starts_at,ends_at,timezone,status,reason_code,checked_in_at,reminders,rescheduled_from,live_request_id,created_at FROM listener_bookings WHERE member_id=? OR listener_id=? ORDER BY starts_at,id",user,user));
        data.put("bookingEvents",db.queryForList("SELECT e.id,e.booking_id,e.previous_status,e.next_status,e.reason_code,e.created_at FROM listener_booking_events e JOIN listener_bookings b ON b.id=e.booking_id WHERE b.member_id=? OR b.listener_id=? ORDER BY e.created_at,e.id",user,user));
        data.put("waitingQueue",db.queryForList("SELECT id,language,reminders,admitted_at,expires_at,status,allocation_id,allocation_expires_at,reason_code,live_request_id FROM listener_wait_queue WHERE member_id=? ORDER BY sequence",user));
        data.put("queueEvents",db.queryForList("SELECT e.id,e.queue_id,e.previous_status,e.next_status,e.reason_code,e.created_at FROM listener_queue_events e JOIN listener_wait_queue q ON q.id=e.queue_id WHERE q.member_id=? ORDER BY e.created_at,e.id",user));
        data.put("sessionDuration",db.queryForList("SELECT id,status,activated_at,planned_seconds,planned_ends_at,extension_seconds_used,ended_at FROM live_support_requests WHERE(member_id=? OR listener_id=?) AND planned_seconds IS NOT NULL ORDER BY created_at,id",user,user));
        data.put("extensionConsents",db.queryForList("SELECT e.id,e.session_id,e.seconds,e.status,e.reason_code,e.original_deadline,e.expires_at,e.member_approved_at,e.listener_approved_at,e.applied_at FROM listener_session_extensions e JOIN live_support_requests r ON r.id=e.session_id WHERE r.member_id=? OR r.listener_id=? ORDER BY e.created_at,e.id",user,user));
        data.put("feedback",db.queryForList("SELECT session_id,helpful,respected,would_request_again,revision,created_at,updated_at FROM listener_session_feedback WHERE owner_id=? ORDER BY updated_at,session_id",user));
        data.put("notices",db.queryForList("SELECT entity_id,kind,created_at FROM listener_support_notices WHERE user_id=? ORDER BY created_at,entity_id",user));
        return data;
    }

    @Transactional
    public void delete(UUID userId, String password) {
        var user = users.findById(userId).orElseThrow(() -> new NotFoundException("Account was not found"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) throw new ForbiddenException("Password is incorrect");
        var keys = repository.professionalDocumentKeys(userId);
        repository.erase(userId);
        for(String key:keys) db.update("INSERT INTO account_file_erasures(user_id,storage_key) VALUES (?,?) ON CONFLICT DO NOTHING",userId,key);
        if(!keys.isEmpty()) db.update("UPDATE account_deletions SET status='PROCESSING',completed_at=NULL WHERE user_id=?",userId);
    }
}
