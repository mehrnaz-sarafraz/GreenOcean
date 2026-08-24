package com.greenocean.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ProfileAccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldUpdateProfileRespectPrivacyAndProtectUsernameUniqueness() throws Exception {
        AuthTokens firstUser = registerAndLogin("profile-one");
        AuthTokens secondUser = registerAndLogin("profile-two");
        String updatedUsername = "ocean" + unique().substring(0, 15);

        mockMvc.perform(get("/api/v1/profiles/me").header("Authorization", auth(firstUser.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countryCode").value("IR"))
                .andExpect(jsonPath("$.city").value("Tehran"));

        mockMvc.perform(patch("/api/v1/profiles/me")
                        .header("Authorization", auth(firstUser.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", updatedUsername,
                                "displayName", "Updated Ocean User",
                                "bio", "A safe profile bio",
                                "showLocation", false,
                                "showBirthYear", false,
                                "profilePrivate", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(updatedUsername))
                .andExpect(jsonPath("$.profilePrivate").value(true));

        mockMvc.perform(get("/api/v1/profiles/{username}", updatedUsername)
                        .header("Authorization", auth(secondUser.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("A safe profile bio"))
                .andExpect(jsonPath("$.countryCode").doesNotExist())
                .andExpect(jsonPath("$.city").doesNotExist())
                .andExpect(jsonPath("$.birthYear").doesNotExist());

        mockMvc.perform(patch("/api/v1/profiles/me")
                        .header("Authorization", auth(secondUser.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", updatedUsername))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldChangePasswordAndRevokeAllRefreshTokens() throws Exception {
        String base = "security-" + unique().substring(0, 12);
        AuthTokens initial = registerAndLogin(base);
        String newPassword = "NewStrongPassword_2026";

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", auth(initial.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", initial.password(),
                                "newPassword", newPassword
                        ))))
                .andExpect(status().isNoContent());

        assertRefreshRejected(initial.refreshToken());
        assertLoginRejected(initial.email(), initial.password());

        AuthTokens afterPasswordChange = login(initial.email(), newPassword);
        mockMvc.perform(post("/api/v1/auth/logout-all")
                        .header("Authorization", auth(afterPasswordChange.accessToken())))
                .andExpect(status().isNoContent());
        assertRefreshRejected(afterPasswordChange.refreshToken());
    }

    private AuthTokens registerAndLogin(String prefix) throws Exception {
        String unique = unique();
        String email = prefix + "-" + unique + "@example.test";
        String password = "StrongPassword_2026";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", password,
                                "username", "u" + unique.substring(0, 20),
                                "displayName", "Profile Test User",
                                "birthYear", 2000,
                                "countryCode", "IR",
                                "city", "Tehran"
                        ))))
                .andExpect(status().isCreated());
        return login(email, password);
    }

    private AuthTokens login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return new AuthTokens(email, password, body.get("accessToken").asText(), body.get("refreshToken").asText());
    }

    private void assertLoginRejected(String email, String password) throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isUnauthorized());
    }

    private void assertRefreshRejected(String refreshToken) throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    private String auth(String accessToken) {
        return "Bearer " + accessToken;
    }

    private String json(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    private String unique() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private record AuthTokens(String email, String password, String accessToken, String refreshToken) {
    }
}
