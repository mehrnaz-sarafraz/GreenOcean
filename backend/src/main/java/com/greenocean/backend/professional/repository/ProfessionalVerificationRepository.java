package com.greenocean.backend.professional.repository;

import com.greenocean.backend.professional.dto.SubmitVerificationRequest;
import com.greenocean.backend.professional.dto.VerificationStatusResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProfessionalVerificationRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    public ProfessionalVerificationRepository(JdbcTemplate jdbc, ObjectMapper json) { this.jdbc = jdbc; this.json = json; }

    public boolean documentsAvailable(UUID userId, List<UUID> documentIds) {
        return documentIds.stream().allMatch(id -> Boolean.TRUE.equals(jdbc.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM media WHERE id = ? AND uploader_id = ? AND purpose = 'VERIFICATION'
                  AND media_status = 'PENDING' AND expires_at > CURRENT_TIMESTAMP)
                """, Boolean.class, id, userId)));
    }

    public void submit(UUID id, UUID userId, SubmitVerificationRequest request) {
        jdbc.update("""
                INSERT INTO professional_profiles (user_id, professional_type, specialization, license_number, country, verification_status)
                VALUES (?, ?, ?, ?, ?, 'PENDING')
                ON CONFLICT (user_id) DO UPDATE SET professional_type = EXCLUDED.professional_type,
                    specialization = EXCLUDED.specialization, license_number = EXCLUDED.license_number,
                    country = EXCLUDED.country, verification_status = 'PENDING', verified_at = NULL
                """, userId, request.professionalType(), normalize(request.specialization()),
                request.licenseNumber().trim(), request.country().trim());
        String snapshot = json.writeValueAsString(java.util.Map.of(
                "professionalType", request.professionalType(), "profession", request.profession().trim(),
                "country", request.country().trim(), "licenseNumber", request.licenseNumber().trim(),
                "specialization", request.specialization() == null ? "" : request.specialization().trim()));
        jdbc.update("""
                INSERT INTO professional_verifications (id, professional_user_id, profession, country, document_names, submitted_profile)
                VALUES (?, ?, ?, ?, ARRAY[]::TEXT[], CAST(? AS JSONB))
                """, id, userId, request.profession().trim(), request.country().trim(), snapshot);
        for (UUID mediaId : request.documentIds()) {
            jdbc.update("INSERT INTO professional_verification_documents (verification_id, media_id, document_kind) VALUES (?, ?, 'CREDENTIAL')",
                    id, mediaId);
            jdbc.update("UPDATE media SET media_status = 'READY', expires_at = NULL WHERE id = ?", mediaId);
        }
    }

    public Optional<VerificationStatusResponse> latest(UUID userId) {
        return jdbc.query("""
                SELECT id, status, profession, country, submitted_at, reviewed_at, notes, info_request
                  FROM professional_verifications WHERE professional_user_id = ? ORDER BY submitted_at DESC LIMIT 1
                """, (rs, row) -> new VerificationStatusResponse(rs.getObject("id", UUID.class), rs.getString("status"),
                rs.getString("profession"), rs.getString("country"), rs.getObject("submitted_at", OffsetDateTime.class).toInstant(),
                rs.getObject("reviewed_at", OffsetDateTime.class) == null ? null : rs.getObject("reviewed_at", OffsetDateTime.class).toInstant(),
                rs.getString("notes"), rs.getString("info_request")), userId).stream().findFirst();
    }

    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
