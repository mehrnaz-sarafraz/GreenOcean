package com.greenocean.backend.auth.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.greenocean.backend.auth.service.AuthTokenDelivery;
import com.greenocean.backend.auth.service.NoOpAuthTokenDelivery;

import java.net.URI;
import java.util.List;

@Component
@Profile("prod")
public class ProductionSecurityValidator {
    private final List<String> allowedOrigins;
    private final AuthTokenDelivery tokenDelivery;

    public ProductionSecurityValidator(
            @Value("${greenocean.security.allowed-origin-patterns}") List<String> allowedOrigins,
            AuthTokenDelivery tokenDelivery
    ) {
        this.allowedOrigins = allowedOrigins;
        this.tokenDelivery = tokenDelivery;
    }

    @PostConstruct
    void validate() {
        if (allowedOrigins.isEmpty()) {
            throw new IllegalStateException("At least one production CORS origin is required");
        }
        for (String origin : allowedOrigins) {
            URI uri;
            try {
                uri = URI.create(origin.trim());
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException("Production CORS origins must be valid HTTPS origins", exception);
            }
            if (origin.contains("*")
                    || !"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getUserInfo() != null
                    || (uri.getPath() != null && !uri.getPath().isEmpty())
                    || uri.getQuery() != null
                    || uri.getFragment() != null) {
                throw new IllegalStateException("Production CORS origins must be explicit HTTPS origins");
            }
        }
        if (tokenDelivery instanceof NoOpAuthTokenDelivery) {
            throw new IllegalStateException("Production auth token delivery must be configured");
        }
    }
}
