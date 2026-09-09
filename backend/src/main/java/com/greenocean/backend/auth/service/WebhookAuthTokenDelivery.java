package com.greenocean.backend.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
@ConditionalOnExpression("'${greenocean.auth.token-delivery.webhook-url:}' != ''")
public class WebhookAuthTokenDelivery implements AuthTokenDelivery {
    private final URI webhookUri;
    private final String webhookSecret;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public WebhookAuthTokenDelivery(
            @Value("${greenocean.auth.token-delivery.webhook-url}") String webhookUrl,
            @Value("${greenocean.auth.token-delivery.webhook-secret:}") String webhookSecret,
            ObjectMapper objectMapper
    ) {
        this.webhookUri = URI.create(webhookUrl);
        if (!"https".equalsIgnoreCase(webhookUri.getScheme())) {
            throw new IllegalStateException("Auth token delivery webhook must use HTTPS");
        }
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException("Auth token delivery webhook secret is required");
        }
        this.webhookSecret = webhookSecret;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    @Override
    public void sendEmailVerification(String email, String token, Instant expiresAt) {
        send("EMAIL_VERIFICATION", email, token, expiresAt);
    }

    @Override
    public void sendPasswordReset(String email, String token, Instant expiresAt) {
        send("PASSWORD_RESET", email, token, expiresAt);
    }

    private void send(String type, String email, String token, Instant expiresAt) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(Map.of(
                    "type", type,
                    "email", email,
                    "token", token,
                    "expiresAt", expiresAt.toString()
            ));
            HttpRequest request = HttpRequest.newBuilder(webhookUri)
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + webhookSecret)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Auth token delivery webhook returned " + response.statusCode());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Auth token delivery was interrupted", exception);
        } catch (Exception exception) {
            if (exception instanceof IllegalStateException illegalStateException) {
                throw illegalStateException;
            }
            throw new IllegalStateException("Auth token delivery failed", exception);
        }
    }
}
