package com.greenocean.backend.auth.config;

import com.greenocean.backend.auth.service.AuthTokenDelivery;
import com.greenocean.backend.auth.service.NoOpAuthTokenDelivery;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionSecurityValidatorTest {
    @Test
    void shouldRejectWildcardOrInsecureProductionOrigins() {
        assertThatThrownBy(() -> validator(List.of("https://*.example.com"), delivery()).validate())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> validator(List.of("http://app.example.com"), delivery()).validate())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectMissingProductionTokenDelivery() {
        assertThatThrownBy(() -> validator(
                List.of("https://app.example.com"), new NoOpAuthTokenDelivery()).validate())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldAcceptExplicitHttpsOriginsWithConfiguredDelivery() {
        assertThatCode(() -> validator(
                List.of("https://app.example.com", "https://admin.example.com"), delivery()).validate())
                .doesNotThrowAnyException();
    }

    private ProductionSecurityValidator validator(List<String> origins, AuthTokenDelivery delivery) {
        return new ProductionSecurityValidator(origins, delivery);
    }

    private AuthTokenDelivery delivery() {
        return new AuthTokenDelivery() {
            @Override
            public void sendEmailVerification(String email, String token, Instant expiresAt) {
            }

            @Override
            public void sendPasswordReset(String email, String token, Instant expiresAt) {
            }
        };
    }
}
