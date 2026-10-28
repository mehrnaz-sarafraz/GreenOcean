package com.greenocean.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class CommunityNotificationIntegrationTest {
    @Autowired private jakarta.persistence.EntityManager fixtureEntities;
    @Autowired private org.springframework.jdbc.core.JdbcTemplate fixtureDb;
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void shouldSupportCommunityMembershipPostsAndNotifications() throws Exception {
        TestUser owner = registerAndLogin("community-owner");
        TestUser member = registerAndLogin("community-member");
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        JsonNode community = createCommunity(owner.token(), "Safe Support " + suffix, "safe-support-" + suffix, false);
        String communityId = community.get("id").asText();

        mockMvc.perform(get("/api/v1/communities").queryParam("q", suffix).header(HttpHeaders.AUTHORIZATION, auth(member.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(communityId))
                .andExpect(jsonPath("$.items[0].member").value(false));

        mockMvc.perform(get("/api/v1/communities/{id}/posts", communityId).header(HttpHeaders.AUTHORIZATION, auth(member.token())))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/v1/communities/{id}/membership", communityId).header(HttpHeaders.AUTHORIZATION, auth(member.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member").value(true))
                .andExpect(jsonPath("$.memberCount").doesNotExist());

        MvcResult postResult = mockMvc.perform(post("/api/v1/posts").header(HttpHeaders.AUTHORIZATION, auth(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "body", "Community support story " + suffix,
                                "publicationIdentity", "SOCIAL_IDENTITY", "anonymous", false, "visibility", "COMMUNITY",
                                "communityId", communityId))))
                .andExpect(status().isCreated()).andReturn();
        String postId = objectMapper.readTree(postResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/communities/{id}/posts", communityId).header(HttpHeaders.AUTHORIZATION, auth(member.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(postId));

        JsonNode privateCommunity = createCommunity(owner.token(), "Private Circle " + suffix,
                "private-circle-" + suffix, true);
        mockMvc.perform(put("/api/v1/communities/{id}/membership", privateCommunity.get("id").asText())
                        .header(HttpHeaders.AUTHORIZATION, auth(member.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member").value(false))
                .andExpect(jsonPath("$.joinRequested").value(true));

        mockMvc.perform(put("/api/v1/social/follows/{id}", owner.userId()).header(HttpHeaders.AUTHORIZATION, auth(member.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FOLLOWING"));
        mockMvc.perform(put("/api/v1/posts/{id}/like", postId).header(HttpHeaders.AUTHORIZATION, auth(member.token())))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/posts/{id}/comments", postId).header(HttpHeaders.AUTHORIZATION, auth(member.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("body", "Real support", "anonymous", false))))
                .andExpect(status().isCreated());

        MvcResult notificationsResult = mockMvc.perform(get("/api/v1/notifications").header(HttpHeaders.AUTHORIZATION, auth(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(3))
                .andReturn();
        JsonNode notifications = objectMapper.readTree(notificationsResult.getResponse().getContentAsString()).get("items");
        Set<String> types = new HashSet<>();
        notifications.forEach(item -> types.add(item.get("type").asText()));
        assertEquals(Set.of("FOLLOW", "LIKE", "COMMENT"), types);
        assertTrue(notifications.get(0).hasNonNull("actor"));

        mockMvc.perform(get("/api/v1/notifications/unread-count").header(HttpHeaders.AUTHORIZATION, auth(owner.token())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.count").value(3));
        mockMvc.perform(put("/api/v1/notifications/{id}/read", notifications.get(0).get("id").asText())
                        .header(HttpHeaders.AUTHORIZATION, auth(owner.token())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/notifications/unread-count").header(HttpHeaders.AUTHORIZATION, auth(owner.token())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.count").value(2));
        mockMvc.perform(put("/api/v1/notifications/read-all").header(HttpHeaders.AUTHORIZATION, auth(owner.token())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/notifications/unread-count").header(HttpHeaders.AUTHORIZATION, auth(owner.token())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.count").value(0));
    }

    private JsonNode createCommunity(String token, String name, String slug, boolean privateCommunity) throws Exception {
        // Historical approved-group fixture; the legacy creation route is covered by M1 denial tests.
        UUID owner=UUID.fromString(objectMapper.readTree(java.util.Base64.getUrlDecoder().decode(token.split("\\.")[1])).path("sub").asText());
        UUID id=UUID.randomUUID();fixtureDb.update("INSERT INTO communities(id,name,slug,description,is_private,created_by) VALUES(?,?,?,'A safe support space',?,?)",id,name,slug,privateCommunity,owner);
        fixtureDb.update("INSERT INTO community_members(community_id,user_id,role) VALUES(?,?,'OWNER')",id,owner);
        return objectMapper.readTree(mockMvc.perform(get("/api/v1/communities/slug/"+slug).header(HttpHeaders.AUTHORIZATION,auth(token))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private TestUser registerAndLogin(String prefix) throws Exception {
        String unique = UUID.randomUUID().toString().replace("-", "");
        String email = prefix + "-" + unique + "@example.test";
        String username = "u" + unique.substring(0, 20);
        String password = "StrongPassword_2026";
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password, "username", username,
                                "displayName", prefix, "birthYear", 2000, "countryCode", "IR", "city", "Tehran", "adultSelfAttested", true, "termsVersion", 1))))
                .andExpect(status().isCreated()).andReturn();
        PhaseAFixtures.activate(fixtureDb, email);
        fixtureEntities.clear();
        fixtureDb.update("UPDATE profiles SET username=?,is_profile_private=FALSE WHERE user_id=(SELECT id FROM users WHERE email=?)",username,email);
        String userId = fixtureDb.queryForObject("SELECT id::text FROM users WHERE email=?",String.class,email);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn();
        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();
        return new TestUser(userId, token);
    }

    private String auth(String token) { return "Bearer " + token; }
    private String json(Object value) { return objectMapper.writeValueAsString(value); }
    private record TestUser(String userId, String token) {}
}
