package com.greenocean.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "greenocean.security.rate-limit.login-ip-max=100",
        "greenocean.security.rate-limit.login-ip-window=PT1M",

        "greenocean.security.rate-limit.login-failure-max=5",
        "greenocean.security.rate-limit.login-lock-duration=PT1M",

        "greenocean.security.rate-limit.register-ip-max=2",
        "greenocean.security.rate-limit.register-ip-window=PT1M",

        "greenocean.security.rate-limit.refresh-ip-max=2",
        "greenocean.security.rate-limit.refresh-ip-window=PT1M"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(
        classes = {
                BackendApplication.class,
                TestcontainersConfiguration.class
        }
)
class AuthRateLimitIntegrationTest {

    private static final String PASSWORD =
            "StrongPassword_2026";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldTemporarilyLockIdentityAfterRepeatedFailedLogins()
            throws Exception {

        String unique = unique();
        String email =
                "brute-force-" + unique + "@example.test";

        String ip = "10.10.0.1";

        register(
                email,
                "u" + unique.substring(0, 20),
                ip
        );

        for (int attempt = 1; attempt <= 5; attempt++) {
            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .with(remoteIp(ip))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json(Map.of(
                                            "email", email,
                                            "password", "WrongPassword_2026",
                                            "deviceName", "security-test"
                                    )))
                    )
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .with(remoteIp(ip))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of(
                                        "email", email,
                                        "password", PASSWORD,
                                        "deviceName", "security-test"
                                )))
                )
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error")
                        .value("Too Many Requests"));
    }

    @Test
    void shouldClearFailedLoginCounterAfterSuccessfulLogin()
            throws Exception {

        String unique = unique();
        String email =
                "reset-failures-" + unique + "@example.test";

        String ip = "10.10.0.2";

        register(
                email,
                "u" + unique.substring(0, 20),
                ip
        );

        for (int attempt = 1; attempt <= 4; attempt++) {
            login(
                    email,
                    "WrongPassword_2026",
                    ip,
                    401
            );
        }

        login(
                email,
                PASSWORD,
                ip,
                200
        );

        for (int attempt = 1; attempt <= 5; attempt++) {
            login(
                    email,
                    "WrongPassword_2026",
                    ip,
                    401
            );
        }

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .with(remoteIp(ip))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of(
                                        "email", email,
                                        "password", PASSWORD,
                                        "deviceName", "security-test"
                                )))
                )
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void shouldRateLimitRegistrationByIp()
            throws Exception {

        String ip = "10.10.0.3";

        String first = unique();

        register(
                "register-one-" + first + "@example.test",
                "u" + first.substring(0, 20),
                ip
        );

        String second = unique();

        register(
                "register-two-" + second + "@example.test",
                "u" + second.substring(0, 20),
                ip
        );

        String third = unique();

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .with(remoteIp(ip))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of(
                                        "email",
                                        "register-three-" + third + "@example.test",

                                        "password",
                                        PASSWORD,

                                        "username",
                                        "u" + third.substring(0, 20),

                                        "displayName",
                                        "Rate Limit Test",

                                        "birthYear",
                                        2000,

                                        "countryCode",
                                        "IR",

                                        "city",
                                        "Tehran"
                                )))
                )
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    void shouldRateLimitRefreshByIp()
            throws Exception {

        String unique = unique();

        String email =
                "refresh-limit-" + unique + "@example.test";

        String ip = "10.10.0.4";

        register(
                email,
                "u" + unique.substring(0, 20),
                ip
        );

        String refreshToken =
                loginAndGetRefreshToken(
                        email,
                        PASSWORD,
                        ip
                );

        String secondRefreshToken =
                refreshAndGetRotatedToken(
                        refreshToken,
                        ip
                );

        String thirdRefreshToken =
                refreshAndGetRotatedToken(
                        secondRefreshToken,
                        ip
                );

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .with(remoteIp(ip))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of(
                                        "refreshToken",
                                        thirdRefreshToken
                                )))
                )
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.status").value(429));
    }

    private void register(
            String email,
            String username,
            String ip
    ) throws Exception {

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .with(remoteIp(ip))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of(
                                        "email", email,
                                        "password", PASSWORD,
                                        "username", username,
                                        "displayName", "Security Test User",
                                        "birthYear", 2000,
                                        "countryCode", "IR",
                                        "city", "Tehran"
                                )))
                )
                .andExpect(status().isCreated());
    }

    private void login(
            String email,
            String password,
            String ip,
            int expectedStatus
    ) throws Exception {

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .with(remoteIp(ip))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of(
                                        "email", email,
                                        "password", password,
                                        "deviceName", "security-test"
                                )))
                )
                .andExpect(
                        status().is(expectedStatus)
                );
    }

    private String loginAndGetRefreshToken(
            String email,
            String password,
            String ip
    ) throws Exception {

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .with(remoteIp(ip))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(json(Map.of(
                                                "email", email,
                                                "password", password,
                                                "deviceName", "security-test"
                                        )))
                        )
                        .andExpect(status().isOk())
                        .andReturn();

        JsonNode body =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );

        return body.get("refreshToken").asText();
    }

    private String refreshAndGetRotatedToken(
            String refreshToken,
            String ip
    ) throws Exception {

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/auth/refresh")
                                        .with(remoteIp(ip))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(json(Map.of(
                                                "refreshToken",
                                                refreshToken
                                        )))
                        )
                        .andExpect(status().isOk())
                        .andReturn();

        JsonNode body =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );

        return body.get("refreshToken").asText();
    }

    private RequestPostProcessor remoteIp(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    private String unique() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "");
    }

    private String json(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}