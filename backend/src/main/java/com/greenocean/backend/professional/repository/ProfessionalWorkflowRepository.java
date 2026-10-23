package com.greenocean.backend.professional.repository;

import com.greenocean.backend.professional.dto.*;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProfessionalWorkflowRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProfessionalWorkflowRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public boolean professionalExists(UUID professionalId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM professional_profiles WHERE user_id = ? AND verification_status = 'VERIFIED')",
                Boolean.class, professionalId));
    }

    public List<ProfessionalReviewResponse> reviews(UUID professionalId, UUID viewerId) {
        return jdbcTemplate.query("""
                SELECT r.id, r.professional_user_id, p.display_name, r.rating, r.body, r.status, r.created_at,
                       r.reviewer_user_id = ? AS mine
                  FROM professional_reviews r
                  JOIN profiles p ON p.user_id = r.reviewer_user_id
                 WHERE r.professional_user_id = ? AND (r.status = 'PUBLISHED' OR r.reviewer_user_id = ?)
                 ORDER BY r.created_at DESC
                """, (rs, row) -> new ProfessionalReviewResponse(
                rs.getObject("id", UUID.class), rs.getObject("professional_user_id", UUID.class),
                rs.getString("display_name"), rs.getInt("rating"), rs.getString("body"),
                rs.getString("status"), rs.getBoolean("mine"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant()), viewerId, professionalId, viewerId);
    }

    public void saveReview(UUID id, UUID professionalId, UUID reviewerId, CreateProfessionalReviewRequest request) {
        jdbcTemplate.update("""
                INSERT INTO professional_reviews (id, professional_user_id, reviewer_user_id, rating, body, status)
                VALUES (?, ?, ?, ?, ?, 'PENDING')
                ON CONFLICT (professional_user_id, reviewer_user_id) DO UPDATE
                    SET rating = EXCLUDED.rating, body = EXCLUDED.body, status = 'PENDING', updated_at = CURRENT_TIMESTAMP
                """, id, professionalId, reviewerId, request.rating(), request.body().trim());
    }

    public List<ConsultationResponse> consultations(UUID userId) {
        return jdbcTemplate.query("""
                SELECT c.id, c.professional_user_id, pp.display_name AS professional_name,
                       c.requester_user_id, rp.display_name AS requester_name, c.scheduled_at, c.consultation_mode,
                       c.note, c.status, c.created_at
                  FROM consultation_requests c
                  JOIN profiles pp ON pp.user_id = c.professional_user_id
                  JOIN profiles rp ON rp.user_id = c.requester_user_id
                 WHERE c.requester_user_id = ?
                 ORDER BY c.scheduled_at DESC
                """, (rs, row) -> new ConsultationResponse(
                rs.getObject("id", UUID.class), rs.getObject("professional_user_id", UUID.class),
                rs.getString("professional_name"), rs.getObject("requester_user_id", UUID.class),
                rs.getString("requester_name"), rs.getObject("scheduled_at", OffsetDateTime.class).toInstant(),
                rs.getString("consultation_mode"), rs.getString("note"), rs.getString("status"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant()), userId);
    }

    public List<ConsultationResponse> professionalConsultations(UUID userId) {
        return jdbcTemplate.query("""
                SELECT c.id, c.professional_user_id, pp.display_name AS professional_name,
                       c.requester_user_id, rp.display_name AS requester_name, c.scheduled_at, c.consultation_mode,
                       c.note, c.status, c.created_at
                  FROM consultation_requests c
                  JOIN profiles pp ON pp.user_id = c.professional_user_id
                  JOIN profiles rp ON rp.user_id = c.requester_user_id
                 WHERE c.professional_user_id = ? ORDER BY c.scheduled_at
                """, (rs, row) -> new ConsultationResponse(
                rs.getObject("id", UUID.class), rs.getObject("professional_user_id", UUID.class),
                rs.getString("professional_name"), rs.getObject("requester_user_id", UUID.class),
                rs.getString("requester_name"), rs.getObject("scheduled_at", OffsetDateTime.class).toInstant(),
                rs.getString("consultation_mode"), rs.getString("note"), rs.getString("status"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant()), userId);
    }

    public List<ProfessionalAvailabilityResponse> availability(UUID professionalId, boolean includeUnavailable) {
        String statusClause = includeUnavailable ? "" : " AND status = 'AVAILABLE'";
        return jdbcTemplate.query("""
                SELECT id, professional_user_id, starts_at, ends_at, consultation_mode, status
                  FROM professional_availability_slots
                 WHERE professional_user_id = ? AND starts_at > CURRENT_TIMESTAMP
                """ + statusClause + " ORDER BY starts_at", this::availabilitySlot, professionalId);
    }

    public Optional<ProfessionalAvailabilityResponse> availableSlotForUpdate(UUID slotId, UUID professionalId) {
        return jdbcTemplate.query("""
                SELECT id, professional_user_id, starts_at, ends_at, consultation_mode, status
                  FROM professional_availability_slots
                 WHERE id = ? AND professional_user_id = ? AND status = 'AVAILABLE'
                   AND starts_at > CURRENT_TIMESTAMP
                 FOR UPDATE
                """, this::availabilitySlot, slotId, professionalId).stream().findFirst();
    }

    public ProfessionalAvailabilityResponse createAvailability(UUID id, UUID professionalId,
                                                                 CreateAvailabilityRequest request) {
        jdbcTemplate.update("""
                INSERT INTO professional_availability_slots
                    (id, professional_user_id, starts_at, ends_at, consultation_mode)
                VALUES (?, ?, ?, ?, ?)
                """, id, professionalId, request.startsAt(), request.endsAt(), request.consultationMode().trim());
        return new ProfessionalAvailabilityResponse(id, professionalId, request.startsAt().toInstant(),
                request.endsAt().toInstant(), request.consultationMode().trim(), "AVAILABLE");
    }

    public void lockAvailabilitySchedule(UUID professionalId) {
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            try (var statement = connection.prepareStatement(
                    "SELECT pg_advisory_xact_lock(hashtextextended(?, 0))")) {
                statement.setString(1, professionalId.toString());
                statement.execute();
            }
            return null;
        });
    }

    public boolean hasAvailabilityOverlap(UUID professionalId, OffsetDateTime startsAt, OffsetDateTime endsAt) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM professional_availability_slots
                               WHERE professional_user_id = ? AND status <> 'CANCELLED'
                                 AND starts_at < ? AND ends_at > ?)
                """, Boolean.class, professionalId, endsAt, startsAt));
    }

    public boolean cancelAvailability(UUID slotId, UUID professionalId) {
        return jdbcTemplate.update("""
                UPDATE professional_availability_slots SET status = 'CANCELLED'
                 WHERE id = ? AND professional_user_id = ? AND status = 'AVAILABLE'
                """, slotId, professionalId) == 1;
    }

    public void bookAvailability(UUID slotId) {
        jdbcTemplate.update("UPDATE professional_availability_slots SET status = 'BOOKED' WHERE id = ?", slotId);
    }

    public void releaseAvailability(UUID consultationId) {
        jdbcTemplate.update("""
                UPDATE professional_availability_slots s SET status = 'AVAILABLE'
                  FROM consultation_requests c
                 WHERE c.id = ? AND c.availability_slot_id = s.id AND s.starts_at > CURRENT_TIMESTAMP
                """, consultationId);
    }

    public boolean updateConsultationStatus(UUID consultationId, UUID professionalId, String status) {
        return jdbcTemplate.update("""
                UPDATE consultation_requests SET status = ?
                 WHERE id = ? AND professional_user_id = ? AND status IN ('REQUESTED', 'CONFIRMED')
                """, status, consultationId, professionalId) == 1;
    }

    public boolean cancelOwnConsultation(UUID consultationId, UUID requesterId) {
        return jdbcTemplate.update("""
                UPDATE consultation_requests SET status = 'CANCELLED'
                 WHERE id = ? AND requester_user_id = ? AND status IN ('REQUESTED', 'CONFIRMED')
                """, consultationId, requesterId) == 1;
    }

    public void createConsultation(UUID id, UUID userId, UUID professionalId,
                                   ProfessionalAvailabilityResponse slot, CreateConsultationRequest request) {
        jdbcTemplate.update("""
                INSERT INTO consultation_requests
                    (id, requester_user_id, professional_user_id, availability_slot_id,
                     scheduled_at, consultation_mode, note)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, id, userId, professionalId, slot.id(), java.sql.Timestamp.from(slot.startsAt()), slot.consultationMode(),
                normalize(request.note()));
    }

    public boolean hasPendingApplication(UUID userId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM professional_verifications
                               WHERE professional_user_id = ? AND status = 'PENDING')
                """, Boolean.class, userId));
    }

    public void submitApplication(UUID verificationId, UUID userId, ProfessionalApplicationRequest request) {
        jdbcTemplate.update("""
                INSERT INTO professional_profiles (
                    user_id, professional_type, title, specialization, bio, years_of_experience,
                    specialties, languages, gender, country, city, workplace, clinic_name, clinic_address,
                    education, license_number, consultation_modes, verification_status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')
                ON CONFLICT (user_id) DO UPDATE SET
                    professional_type = EXCLUDED.professional_type, title = EXCLUDED.title,
                    specialization = EXCLUDED.specialization, bio = EXCLUDED.bio,
                    years_of_experience = EXCLUDED.years_of_experience, specialties = EXCLUDED.specialties,
                    languages = EXCLUDED.languages, gender = EXCLUDED.gender, country = EXCLUDED.country,
                    city = EXCLUDED.city, workplace = EXCLUDED.workplace, clinic_name = EXCLUDED.clinic_name,
                    clinic_address = EXCLUDED.clinic_address, education = EXCLUDED.education,
                    license_number = EXCLUDED.license_number, consultation_modes = EXCLUDED.consultation_modes,
                    verification_status = 'PENDING', verified_at = NULL
                """, userId, request.professionalType().toUpperCase(), request.title().trim(),
                request.specialization().trim(), request.bio().trim(), request.yearsOfExperience(),
                clean(request.specialties()), clean(request.languages()), normalize(request.gender()),
                request.country().trim(), normalize(request.city()), normalize(request.workplace()),
                normalize(request.clinicName()), normalize(request.clinicAddress()), clean(request.education()),
                request.licenseNumber().trim(), clean(request.consultationModes()));
        jdbcTemplate.update("""
                INSERT INTO professional_verifications
                    (id, professional_user_id, profession, country, document_names)
                VALUES (?, ?, ?, ?, ?)
                """, verificationId, userId, request.title().trim(), request.country().trim(),
                clean(request.documentNames()));
    }

    public Optional<ProfessionalApplicationResponse> latestApplication(UUID userId) {
        return jdbcTemplate.query("""
                SELECT id, profession, country, document_names, status, notes, submitted_at, reviewed_at
                  FROM professional_verifications WHERE professional_user_id = ?
                 ORDER BY submitted_at DESC LIMIT 1
                """, (rs, row) -> {
            OffsetDateTime reviewed = rs.getObject("reviewed_at", OffsetDateTime.class);
            UUID applicationId = rs.getObject("id", UUID.class);
            return new ProfessionalApplicationResponse(applicationId, rs.getString("profession"),
                    rs.getString("country"), strings(rs.getArray("document_names")), documents(applicationId), rs.getString("status"),
                    rs.getString("notes"), rs.getObject("submitted_at", OffsetDateTime.class).toInstant(),
                    reviewed == null ? null : reviewed.toInstant());
        }, userId).stream().findFirst();
    }

    public boolean applicationOwnedBy(UUID applicationId, UUID userId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM professional_verifications
                               WHERE id = ? AND professional_user_id = ? AND status = 'PENDING')
                """, Boolean.class, applicationId, userId));
    }

    public ProfessionalDocumentResponse addDocument(UUID id, UUID applicationId,
            com.greenocean.backend.professional.service.ProfessionalDocumentStorage.StoredDocument document) {
        jdbcTemplate.update("""
                INSERT INTO professional_application_documents
                    (id, application_id, original_name, content_type, byte_size, sha256, storage_key)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, id, applicationId, document.originalName(), document.contentType(), document.byteSize(),
                document.sha256(), document.storageKey());
        return documents(applicationId).stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
    }

    private List<ProfessionalDocumentResponse> documents(UUID applicationId) {
        return jdbcTemplate.query("""
                SELECT id, original_name, content_type, byte_size, sha256, scan_status, uploaded_at
                  FROM professional_application_documents WHERE application_id = ? ORDER BY uploaded_at
                """, (rs, row) -> new ProfessionalDocumentResponse(
                rs.getObject("id", UUID.class), rs.getString("original_name"), rs.getString("content_type"),
                rs.getLong("byte_size"), rs.getString("sha256"), rs.getString("scan_status"),
                rs.getObject("uploaded_at", OffsetDateTime.class).toInstant()), applicationId);
    }

    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private ProfessionalAvailabilityResponse availabilitySlot(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
        return new ProfessionalAvailabilityResponse(rs.getObject("id", UUID.class),
                rs.getObject("professional_user_id", UUID.class),
                rs.getObject("starts_at", OffsetDateTime.class).toInstant(),
                rs.getObject("ends_at", OffsetDateTime.class).toInstant(),
                rs.getString("consultation_mode"), rs.getString("status"));
    }
    private String[] clean(List<String> values) {
        return values == null ? new String[0] : values.stream().map(String::trim)
                .filter(value -> !value.isBlank()).distinct().toArray(String[]::new);
    }
    private List<String> strings(Array array) throws SQLException {
        return array == null ? List.of() : Arrays.asList((String[]) array.getArray());
    }
}
