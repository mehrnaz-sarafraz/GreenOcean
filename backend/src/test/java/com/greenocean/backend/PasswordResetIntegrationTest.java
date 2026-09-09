package com.greenocean.backend;

import com.greenocean.backend.auth.service.AuthTokenDelivery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, PasswordResetIntegrationTest.DeliveryConfiguration.class})
class PasswordResetIntegrationTest {
    private static final String OLD_PASSWORD = "StrongPassword_2026";
    private static final String NEW_PASSWORD = "EvenStrongerPassword_2026";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CapturingDelivery delivery;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void shouldResetPasswordRevokeSessionsConsumeTokenAndWriteAudit() throws Exception {
        String unique = UUID.randomUUID().toString().replace("-", "");
        String email = "password-reset-" + unique + "@example.test";
        register(email, "u" + unique.substring(0, 20));
        String refreshToken = login(email, OLD_PASSWORD, 200);

        mockMvc.perform(post("/api/v1/auth/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email))))
                .andExpect(status().isNoContent());

        String resetToken = delivery.passwordResetTokens.get(email);
        assertThat(resetToken).isNotBlank();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM password_reset_tokens WHERE token_hash = ?",
                Integer.class, resetToken)).isZero();

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", resetToken, "newPassword", NEW_PASSWORD))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
        login(email, OLD_PASSWORD, 401);
        assertThat(login(email, NEW_PASSWORD, 200)).isNotBlank();

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", resetToken, "newPassword", OLD_PASSWORD))))
                .andExpect(status().isUnauthorized());

        Integer auditCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM audit_logs a
                JOIN users u ON u.id = a.actor_user_id
                WHERE LOWER(u.email) = LOWER(?) AND a.action = 'PASSWORD_RESET_COMPLETED'
                """, Integer.class, email);
        assertThat(auditCount).isEqualTo(1);
    }

    @Test
    void shouldNotRevealWhetherRecoveryEmailExists() throws Exception {
        mockMvc.perform(post("/api/v1/auth/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "missing-" + UUID.randomUUID() + "@example.test"))))
                .andExpect(status().isNoContent());
    }

    private void register(String email, String username) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", OLD_PASSWORD,
                                "username", username,
                                "displayName", "Password Reset Test",
                                "birthYear", 2000,
                                "countryCode", "IR",
                                "city", "Tehran"
                        ))))
                .andExpect(status().isCreated());
    }

    private String login(String email, String password, int expectedStatus) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", password,
                                "deviceName", "password-reset-test"
                        ))))
                .andExpect(status().is(expectedStatus))
                .andReturn();
        if (expectedStatus != 200) return "";
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("refreshToken").asText();
    }

    private String json(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    @TestConfiguration
    static class DeliveryConfiguration {
        @Bean
        @Primary
        CapturingDelivery capturingDelivery() {
            return new CapturingDelivery();
        }
    }

    static class CapturingDelivery implements AuthTokenDelivery {
        private final Map<String, String> passwordResetTokens = new ConcurrentHashMap<>();

        @Override
        public void sendEmailVerification(String email, String token, Instant expiresAt) {
            // Verification delivery is outside this test's assertion scope.
        }

        @Override
        public void sendPasswordReset(String email, String token, Instant expiresAt) {
            passwordResetTokens.put(email, token);
        }
    }
}
