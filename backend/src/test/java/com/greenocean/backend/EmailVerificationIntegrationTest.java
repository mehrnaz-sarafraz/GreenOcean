package com.greenocean.backend;

import com.greenocean.backend.auth.entity.EmailVerificationToken;
import com.greenocean.backend.auth.entity.User;
import com.greenocean.backend.auth.repository.EmailVerificationTokenRepository;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.EmailVerificationService;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class EmailVerificationIntegrationTest {

    private static final String PASSWORD =
            "StrongPassword_2026";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private DatabaseUuidGenerator databaseUuidGenerator;

    @Test
    void shouldCreateVerificationTokenWhenUserRegisters()
            throws Exception {

        long tokensBefore = tokenRepository.count();

        String unique = unique();

        String email =
                "verification-create-" + unique + "@example.test";

        register(
                email,
                "u" + unique.substring(0, 20)
        );

        long tokensAfter = tokenRepository.count();

        assertThat(tokensAfter)
                .isEqualTo(tokensBefore + 1);

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow();

        assertThat(user.isEmailVerified())
                .isFalse();
    }

    @Test
    void shouldVerifyEmailWithValidToken()
            throws Exception {

        String unique = unique();

        String email =
                "verification-valid-" + unique + "@example.test";

        register(
                email,
                "u" + unique.substring(0, 20)
        );

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow();

        String rawToken =
                emailVerificationService.createToken(user);

        mockMvc.perform(
                        post("/api/v1/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of(
                                        "token",
                                        rawToken
                                )))
                )
                .andExpect(status().isNoContent());

        User verifiedUser = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow();

        assertThat(verifiedUser.isEmailVerified())
                .isTrue();
    }

    @Test
    void shouldRejectVerificationTokenAfterItHasBeenUsed()
            throws Exception {

        String unique = unique();

        String email =
                "verification-single-use-"
                        + unique
                        + "@example.test";

        register(
                email,
                "u" + unique.substring(0, 20)
        );

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow();

        String rawToken =
                emailVerificationService.createToken(user);

        mockMvc.perform(
                        post("/api/v1/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of(
                                        "token",
                                        rawToken
                                )))
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        post("/api/v1/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of(
                                        "token",
                                        rawToken
                                )))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401));
    }

    @Test
    void shouldRejectExpiredVerificationToken()
            throws Exception {

        String unique = unique();

        String email =
                "verification-expired-"
                        + unique
                        + "@example.test";

        register(
                email,
                "u" + unique.substring(0, 20)
        );

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow();

        String rawToken =
                "expired-" + UUID.randomUUID();

        EmailVerificationToken expiredToken =
                new EmailVerificationToken(
                        databaseUuidGenerator.nextUuid(),
                        user,
                        sha256(rawToken),
                        Instant.now().minusSeconds(60)
                );

        tokenRepository.saveAndFlush(expiredToken);

        mockMvc.perform(
                        post("/api/v1/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of(
                                        "token",
                                        rawToken
                                )))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Email verification token is invalid or expired"
                        ));

        User unchangedUser = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow();

        assertThat(unchangedUser.isEmailVerified())
                .isFalse();
    }

    @Test
    void shouldRejectInvalidVerificationToken()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of(
                                        "token",
                                        "this-token-does-not-exist"
                                )))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Email verification token is invalid or expired"
                        ));
    }

    @Test
    void shouldNotRevealWhetherEmailExistsWhenResendingVerification()
            throws Exception {

        String unknownEmail =
                "unknown-" + unique() + "@example.test";

        mockMvc.perform(
                        post("/api/v1/auth/resend-verification")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of(
                                        "email",
                                        unknownEmail
                                )))
                )
                .andExpect(status().isNoContent());

        String unique = unique();

        String existingEmail =
                "verification-resend-"
                        + unique
                        + "@example.test";

        register(
                existingEmail,
                "u" + unique.substring(0, 20)
        );

        mockMvc.perform(
                        post("/api/v1/auth/resend-verification")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of(
                                        "email",
                                        existingEmail
                                )))
                )
                .andExpect(status().isNoContent());
    }

    private void register(
            String email,
            String username
    ) throws Exception {

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .with(request -> {
                                    request.setRemoteAddr(
                                            "192.168."
                                                    + randomOctet()
                                                    + "."
                                                    + randomOctet()
                                    );
                                    return request;
                                })
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of(
                                        "email",
                                        email,

                                        "password",
                                        PASSWORD,

                                        "username",
                                        username,

                                        "displayName",
                                        "Email Verification Test",

                                        "birthYear",
                                        2000,

                                        "countryCode",
                                        "IR",

                                        "city",
                                        "Tehran"
                                )))
                )
                .andExpect(status().isCreated());
    }

    private String sha256(String value)
            throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash =
                digest.digest(
                        value.getBytes(StandardCharsets.UTF_8)
                );

        return HexFormat.of().formatHex(hash);
    }

    private String json(Object value)
            throws Exception {

        return objectMapper.writeValueAsString(value);
    }

    private String unique() {

        return UUID.randomUUID()
                .toString()
                .replace("-", "");
    }

    private int randomOctet() {

        return 1
                + Math.abs(
                UUID.randomUUID()
                        .hashCode()
                        % 250
        );
    }
}