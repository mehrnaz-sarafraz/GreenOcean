package com.greenocean.backend.auth.config;

import com.greenocean.backend.auth.service.AuthTokenDelivery;
import com.greenocean.backend.auth.service.NoOpAuthTokenDelivery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthTokenDeliveryConfig {
    @Bean
    @ConditionalOnMissingBean(AuthTokenDelivery.class)
    AuthTokenDelivery authTokenDelivery() {
        return new NoOpAuthTokenDelivery();
    }
}
