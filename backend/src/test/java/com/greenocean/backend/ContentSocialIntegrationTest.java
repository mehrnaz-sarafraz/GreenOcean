package com.greenocean.backend;
import org.springframework.context.annotation.Import;
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

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ContentSocialIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void shouldEnforceFeedVisibilityAndSupportInteractions() throws Exception {
        TestUser author = registerAndLogin("content-author");
        TestUser reader = registerAndLogin("content-reader");

        String publicPostId = createPost(author.token(), "PUBLIC", "A searchable ocean story");
        String followersPostId = createPost(author.token(), "FOLLOWERS", "Followers-only support story");

        mockMvc.perform(get("/api/v1/posts/feed").header(HttpHeaders.AUTHORIZATION, auth(reader.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(publicPostId));

        mockMvc.perform(put("/api/v1/social/follows/{target}", author.userId()).header(HttpHeaders.AUTHORIZATION, auth(reader.token())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/posts/feed").header(HttpHeaders.AUTHORIZATION, auth(reader.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2));

        mockMvc.perform(put("/api/v1/posts/{id}/like", publicPostId).header(HttpHeaders.AUTHORIZATION, auth(reader.token())))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/api/v1/posts/{id}/bookmark", publicPostId).header(HttpHeaders.AUTHORIZATION, auth(reader.token())))
                .andExpect(status().isNoContent());

        MvcResult commentResult = mockMvc.perform(post("/api/v1/posts/{id}/comments", publicPostId)
                        .header(HttpHeaders.AUTHORIZATION, auth(reader.token())).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("body", "You are not alone", "anonymous", false))))
                .andExpect(status().isCreated())
                .andReturn();
        String commentId = objectMapper.readTree(commentResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/v1/posts/{id}/comments", publicPostId)
                        .header(HttpHeaders.AUTHORIZATION, auth(author.token())).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("body", "Thank you", "parentCommentId", commentId, "anonymous", false))))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/v1/comments/{id}/like", commentId).header(HttpHeaders.AUTHORIZATION, auth(author.token())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/posts/{id}", publicPostId).header(HttpHeaders.AUTHORIZATION, auth(reader.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.bookmarked").value(true))
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.commentCount").value(2));

        mockMvc.perform(get("/api/v1/search/posts").queryParam("q", "ocean").header(HttpHeaders.AUTHORIZATION, auth(reader.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(publicPostId));

        mockMvc.perform(delete("/api/v1/posts/{id}", followersPostId).header(HttpHeaders.AUTHORIZATION, auth(reader.token())))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/social/blocks/{target}", author.userId()).header(HttpHeaders.AUTHORIZATION, auth(reader.token())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/posts/feed").header(HttpHeaders.AUTHORIZATION, auth(reader.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
        mockMvc.perform(get("/api/v1/search/users").queryParam("q", author.username()).header(HttpHeaders.AUTHORIZATION, auth(reader.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));

        mockMvc.perform(delete("/api/v1/posts/{id}", followersPostId).header(HttpHeaders.AUTHORIZATION, auth(author.token())))
                .andExpect(status().isNoContent());
    }

    private String createPost(String token, String visibility, String body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/posts")
                        .header(HttpHeaders.AUTHORIZATION, auth(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("body", body, "anonymous", false, "visibility", visibility))))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private TestUser registerAndLogin(String prefix) throws Exception {
        String unique = UUID.randomUUID().toString().replace("-", "");
        String email = prefix + "-" + unique + "@example.test";
        String username = "u" + unique.substring(0, 20);
        String password = "StrongPassword_2026";
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password, "username", username,
                                "displayName", prefix, "birthYear", 2000, "countryCode", "IR", "city", "Tehran"))))
                .andExpect(status().isCreated()).andReturn();
        String userId = objectMapper.readTree(registerResult.getResponse().getContentAsString()).get("userId").asText();
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn();
        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();
        return new TestUser(userId, username, token);
    }

    private String auth(String token) { return "Bearer " + token; }
    private String json(Object value) { return objectMapper.writeValueAsString(value); }
    private record TestUser(String userId, String username, String token) {}
}
