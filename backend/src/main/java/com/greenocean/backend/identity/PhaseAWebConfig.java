package com.greenocean.backend.identity;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class PhaseAWebConfig implements WebMvcConfigurer {
    private final PhaseAInterceptor interceptor;
    public PhaseAWebConfig(PhaseAInterceptor interceptor) { this.interceptor=interceptor; }
    public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(interceptor); }
}
