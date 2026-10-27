package com.greenocean.backend;

import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.professional.dto.CreateAvailabilityRequest;
import com.greenocean.backend.professional.dto.CreateConsultationRequest;
import com.greenocean.backend.professional.service.ProfessionalWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ProfessionalAvailabilityIntegrationTest {
    @Autowired private jakarta.persistence.EntityManager fixtureEntities;
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private ProfessionalWorkflowService workflowService;

    @Test
    void shouldBookOnePublishedSlotOnlyOnceAndReleaseItAfterCancellation() throws Exception {
        UUID professionalId = register("availability-professional");
        UUID firstMemberId = register("availability-member-one");
        UUID secondMemberId = register("availability-member-two");
        jdbcTemplate.update("""
                INSERT INTO professional_profiles
                    (user_id, professional_type, title, verification_status, verified_at)
                VALUES (?, 'PSYCHOLOGIST', 'Clinical Psychologist', 'VERIFIED', CURRENT_TIMESTAMP)
                """, professionalId);
        jdbcTemplate.update("INSERT INTO regional_services(country_code,service,status) VALUES ('IR','PROFESSIONAL_CARE','AVAILABLE')");
        jdbcTemplate.update("INSERT INTO professional_care_regions(professional_user_id,country_code,approved_by,expires_at) VALUES (?,'IR',?,CURRENT_TIMESTAMP+INTERVAL '1 day')",professionalId,professionalId);

        OffsetDateTime startsAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(2);
        var slot = workflowService.createAvailability(professionalId,
                new CreateAvailabilityRequest(startsAt, startsAt.plusMinutes(50), "VIDEO"));
        assertThrows(ConflictException.class, () -> workflowService.createAvailability(professionalId,
                new CreateAvailabilityRequest(startsAt.plusMinutes(30), startsAt.plusMinutes(80), "VIDEO")));
        var consultation = workflowService.requestConsultation(professionalId, firstMemberId,
                new CreateConsultationRequest(slot.id(), null, null, "Initial consultation"));

        assertEquals("REQUESTED", consultation.status());
        assertThrows(ConflictException.class, () -> workflowService.requestConsultation(
                professionalId, secondMemberId,
                new CreateConsultationRequest(slot.id(), null, null, "Collision")));

        workflowService.cancelConsultation(consultation.id(), firstMemberId);
        assertEquals("AVAILABLE", workflowService.availability(professionalId, true).getFirst().status());
    }

    private UUID register(String label) throws Exception {
        String unique = UUID.randomUUID().toString().replace("-", "");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", label + "-" + unique + "@example.test",
                                "password", "StrongPassword_2026",
                                "username", "u" + unique.substring(0, 20),
                                "displayName", label,
                                "birthYear", 2000,
                                "countryCode", "IR",
                                "city", "Tehran", "adultSelfAttested", true, "termsVersion", 1))))
                .andExpect(status().isCreated())
                .andReturn();
        PhaseAFixtures.activate(jdbcTemplate, label + "-" + unique + "@example.test");
        fixtureEntities.clear();
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE email=?",UUID.class,label + "-" + unique + "@example.test");
    }
}
