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
                listenerPlanning(userId),privateWellbeing(userId),professionalCare(userId));
    }

    private java.util.Map<String,Object> professionalCare(UUID user){
        var result=new java.util.LinkedHashMap<String,Object>();
        result.put("qualifications",db.queryForList("SELECT id,qualification_state,qualification_revision,professional_category,verified_name,license_issuer,license_identifier,verification_method,verification_source,evidence_reference,valid_from,valid_until,recheck_at,reviewed_at FROM professional_verifications WHERE professional_user_id=? ORDER BY submitted_at,id",user));
        result.put("decisions",db.queryForList("SELECT d.id,d.revision,d.state,d.reason,d.evidence_snapshot::text AS evidence_snapshot,d.created_at FROM professional_qualification_decisions d JOIN professional_verifications v ON v.id=d.application_id WHERE v.professional_user_id=? ORDER BY d.created_at,d.id",user));
        result.put("profileHistory",db.queryForList("SELECT revision,title,bio,languages,service_description,conditions_of_service,created_at FROM professional_profile_history WHERE professional_id=? ORDER BY revision",user));
        result.put("calendarRules",db.queryForList("SELECT * FROM professional_calendar_rules WHERE professional_id=? ORDER BY id",user));
        result.put("calendarExceptions",db.queryForList("SELECT id,starts_at,ends_at,reason,active FROM professional_calendar_exceptions WHERE professional_id=? ORDER BY id",user));
        result.put("availability",db.queryForList("SELECT id,starts_at,ends_at,consultation_mode,status,price_units,currency,timezone,revision FROM professional_availability_slots WHERE professional_user_id=? ORDER BY starts_at,id",user));
        result.put("reminderPreferences",db.queryForList("SELECT appointment_id,enabled,updated_at FROM professional_appointment_reminder_preferences WHERE user_id=? ORDER BY appointment_id",user));
        result.put("readiness",db.queryForList("SELECT appointment_id,observed_at,expires_at FROM professional_appointment_readiness WHERE actor_id=? ORDER BY appointment_id",user));
        result.put("legacyUnverifiedFeedback",db.queryForList("SELECT id,professional_user_id,rating,body,status,created_at FROM professional_reviews WHERE reviewer_user_id=? ORDER BY created_at,id",user));
        result.put("clinicAppointments",db.queryForList("SELECT id,scheduled_at,ends_at,status,price_units,currency,terms_snapshot FROM consultation_requests WHERE clinic_governed AND(requester_user_id=? OR professional_user_id=?) ORDER BY id",user,user));
        result.put("clinicFinancialEvents",db.queryForList("SELECT f.* FROM clinic_financial_events f JOIN consultation_requests c ON c.id=f.appointment_id WHERE c.requester_user_id=? OR c.professional_user_id=? ORDER BY f.created_at,f.id",user,user));
        result.put("clinicRefunds",db.queryForList("SELECT r.* FROM clinic_refunds r JOIN consultation_requests c ON c.id=r.appointment_id WHERE c.requester_user_id=? ORDER BY r.id",user));
        result.put("clinicRightsRequests",db.queryForList("SELECT r.id,r.appointment_id,r.category,r.request_kind,r.created_at,e.state,e.evidence_reference FROM clinic_rights_requests r JOIN clinic_rights_events e ON e.request_id=r.id WHERE r.requester_id=? ORDER BY e.created_at,e.id",user));
        result.put("clinicFollowupMetadata",db.queryForList("SELECT f.id,f.appointment_id,v.revision,v.suggested_after,v.expires_at FROM clinic_followups f JOIN clinic_followup_versions v ON v.followup_id=f.id WHERE f.patient_id=? OR f.professional_id=? ORDER BY f.created_at,v.revision",user,user));
        result.put("clinicFeedbackChoices",db.queryForList("SELECT f.id,f.appointment_id,f.state,v.revision,v.rating,v.include_aggregate,v.publish_text FROM clinic_feedback f JOIN clinic_feedback_versions v ON v.feedback_id=f.id WHERE f.patient_id=? ORDER BY f.id,v.revision",user));
        result.put("clinicProtectedContentAccess","Clinical content uses current category access and fresh authentication through /api/v1/clinical; follow-up through /api/v1/clinic-integration/followups; exceptional exports through subject rights requests. No clinical narrative is copied into social/wellbeing archives.");
        return (java.util.Map<String,Object>)com.greenocean.backend.professional.service.ProfessionalAuthority.jsonSafe(result);
    }

    private java.util.Map<String,Object> privateWellbeing(UUID user){
        var data=new java.util.LinkedHashMap<String,Object>();
        for(String table:java.util.List.of("wellbeing_journal_entries","wellbeing_check_ins","wellbeing_goals","wellbeing_reflections","wellbeing_daily_sets"))
            data.put(table,db.queryForList("SELECT to_jsonb(t)::text AS record FROM "+table+" t WHERE user_id=? ORDER BY id",user));
        data.put("goalSteps",db.queryForList("SELECT s.id,s.goal_id,s.title,s.completed,s.position FROM wellbeing_goal_steps s JOIN wellbeing_goals g ON g.id=s.goal_id WHERE g.user_id=? ORDER BY s.goal_id,s.position",user));
        data.put("dailyAnswers",db.queryForList("SELECT s.local_date,s.timezone,q.position,p.id AS prompt_id,p.revision AS prompt_revision,p.body AS question,a.body AS answer,a.revision,a.updated_at FROM wellbeing_daily_sets s JOIN wellbeing_daily_questions q ON q.set_id=s.id JOIN wellbeing_prompt_versions p ON p.id=q.prompt_id JOIN wellbeing_daily_answers a ON a.set_id=q.set_id AND a.position=q.position WHERE s.user_id=? ORDER BY s.local_date,q.position",user));
        data.put("journalDraft",db.queryForList("SELECT title,body,mood,revision,updated_at,expires_at FROM wellbeing_journal_drafts WHERE user_id=? AND expires_at>CURRENT_TIMESTAMP",user));
        data.put("resourceSaves",db.queryForList("SELECT resource_id,created_at FROM growth_resource_saves WHERE user_id=? ORDER BY created_at",user));
        data.put("educationalPermissions",db.queryForList("SELECT action,scope,evidence,expires_at,revoked_at FROM educational_permissions WHERE user_id=? ORDER BY created_at",user));
        data.put("educationalSubmissions",db.queryForList("SELECT v.resource_id,v.revision,v.document::text,v.state,v.review_note,v.reviewed_at,v.published_at FROM growth_resources r JOIN growth_resource_versions v ON v.resource_id=r.id WHERE r.author_id=? ORDER BY r.id,v.revision",user));
        data.put("ownEditorialDecisions",db.queryForList("SELECT resource_id,revision,action,note,created_at FROM growth_editorial_events WHERE actor_id=? ORDER BY created_at,id",user));
        data.put("resourceProposals",db.queryForList("SELECT id,post_id,resource_id,resource_revision,status,reason,created_at FROM post_resource_attachments WHERE proposed_by=? ORDER BY created_at",user));
        data.put("growthRewards",db.queryForList("SELECT id,source_type,completion_id,course_key,activity_key,rule_version,point_delta,reversal_of,reason,created_at FROM growth_reward_ledger WHERE owner_id=? ORDER BY created_at,id",user));
        data.put("learningBadges",db.queryForList("SELECT code,earned_at,revoked_at FROM growth_learning_badges WHERE owner_id=? ORDER BY earned_at,code",user));
        data.put("lessonCompletions",db.queryForList("SELECT id,source_course_key,course_revision,lesson_key,content_digest,lesson_title,completed_at FROM learning_lesson_completions WHERE owner_id=? ORDER BY completed_at,id",user));
        data.put("courseCompletions",db.queryForList("SELECT id,source_course_key,course_revision,course_title,completed_at FROM learning_course_completions WHERE owner_id=? ORDER BY completed_at,id",user));
        data.put("exerciseCompletions",db.queryForList("SELECT id,source_course_key,course_revision,lesson_key,content_digest,exercise_digest,completed_at FROM learning_exercise_completions WHERE owner_id=? ORDER BY completed_at,id",user));
        data.put("pendingLessonOpenings",db.queryForList("SELECT course_id,lesson_key,revision,opened_at FROM learning_lesson_views WHERE owner_id=? ORDER BY opened_at",user));
        data.put("editorialQuestions",db.queryForList("SELECT id,prompt_key,revision,body,language,review_state,review_note,reviewed_at FROM wellbeing_prompt_versions WHERE author_id=? OR reviewer_id=? ORDER BY created_at,id",user,user));
        data.put("legacyLearningProgress",db.queryForList("SELECT path_id,legacy_progress,started_at,updated_at FROM user_growth_path_progress WHERE user_id=? ORDER BY path_id",user));
        return data;
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
