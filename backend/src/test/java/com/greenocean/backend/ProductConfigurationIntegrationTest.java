package com.greenocean.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ProductConfigurationIntegrationTest {
    @Autowired private jakarta.persistence.EntityManager fixtureEntities;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void shouldPersistRegistrationIntentOnboardingAndSearchHistoryInTheAccount() throws Exception {
        String token = registerAndLogin("MEMBER");

        mockMvc.perform(get("/api/v1/preferences/me").header(HttpHeaders.AUTHORIZATION, auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intendedRole").value("MEMBER"))
                .andExpect(jsonPath("$.onboardingComplete").value(false));

        mockMvc.perform(patch("/api/v1/preferences/me")
                        .header(HttpHeaders.AUTHORIZATION, auth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "supportTopics", List.of("anxiety", "sleep"),
                                "supportStyle", "LISTENER",
                                "onboardingComplete", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supportTopics.length()").value(2))
                .andExpect(jsonPath("$.supportStyle").value("LISTENER"))
                .andExpect(jsonPath("$.onboardingComplete").value(true));

        remember(token, "anxiety");
        remember(token, "Anxiety");

        mockMvc.perform(get("/api/v1/search/history").header(HttpHeaders.AUTHORIZATION, auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].term").value("Anxiety"));

        mockMvc.perform(get("/api/v1/account/archive").header(HttpHeaders.AUTHORIZATION, auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferences.onboardingComplete").value(true))
                .andExpect(jsonPath("$.searchHistory.length()").value(1));

        mockMvc.perform(delete("/api/v1/search/history").header(HttpHeaders.AUTHORIZATION, auth(token)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/search/history").header(HttpHeaders.AUTHORIZATION, auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldExposeDatabaseBackedCatalogTrainingAndVerifiedCrisisResourcesWithoutDemoAccounts() throws Exception {
        String token = registerAndLogin("MEMBER");

        MvcResult optionsResult = mockMvc.perform(get("/api/v1/catalog/options")
                        .header(HttpHeaders.AUTHORIZATION, auth(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode options = objectMapper.readTree(optionsResult.getResponse().getContentAsString());
        assertThat(options.size()).isGreaterThanOrEqualTo(60);
        assertThat(options.valueStream().map(node -> node.get("group").asText()).distinct().count())
                .isGreaterThanOrEqualTo(10);

        mockMvc.perform(get("/api/v1/catalog/options")
                        .queryParam("group", "POST_MOOD")
                        .header(HttpHeaders.AUTHORIZATION, auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        MvcResult crisisResult = mockMvc.perform(get("/api/v1/crisis-resources"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode resources = objectMapper.readTree(crisisResult.getResponse().getContentAsString());
        assertThat(resources.size()).isEqualTo(7);
        assertThat(resources.valueStream().allMatch(resource ->
                resource.hasNonNull("sourceUrl") && resource.hasNonNull("lastVerifiedOn"))).isTrue();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email LIKE '%@greenocean.invalid'", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM listener_training_modules WHERE is_active = TRUE", Integer.class)).isEqualTo(5);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM catalog_options WHERE is_active = TRUE", Integer.class)).isGreaterThanOrEqualTo(60);
    }

    private void remember(String token, String term) throws Exception {
        mockMvc.perform(post("/api/v1/search/history")
                        .header(HttpHeaders.AUTHORIZATION, auth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("term", term))))
                .andExpect(status().isOk());
    }

    private String registerAndLogin(String intendedRole) throws Exception {
        String unique = UUID.randomUUID().toString().replace("-", "");
        String email = "configuration-" + unique + "@example.test";
        String password = "StrongPassword_2026";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", password,
                                "username", "u" + unique.substring(0, 20),
                                "displayName", "Configuration Test User",
                                "birthYear", 2000,
                                "countryCode", "IR",
                                "city", "Tehran", "adultSelfAttested", true, "termsVersion", 1,
                                "intendedRole", intendedRole
                        ))))
                .andExpect(status().isCreated());
        PhaseAFixtures.activate(jdbcTemplate, email);
        fixtureEntities.clear();

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String auth(String token) {
        return "Bearer " + token;
    }

    private String json(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
