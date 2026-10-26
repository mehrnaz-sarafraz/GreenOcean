package com.greenocean.backend.identity;
import com.greenocean.backend.auth.service.PasswordPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfigurationSource;

@Component @Profile("prod")
public class ProductionPhaseAValidator {
    public ProductionPhaseAValidator(@Value("${greenocean.mail.enabled:false}") boolean mail,
       @Value("${greenocean.mail.gateway-url:}") String gateway,@Value("${greenocean.mail.outbox-key:}") String outboxKey,
       @Value("${greenocean.auth.jwt-secret}") String jwtKey,@Value("${greenocean.security.admin-enabled:false}") boolean admin,
       @Value("${greenocean.auth.web-cookie-secure:true}") boolean cookie,
       @Value("${greenocean.phase-a.legacy-grace-until}") String legacyGrace,
       @Value("${greenocean.security.allowed-origin-patterns}") java.util.List<String> origins) {
        if(!mail || gateway.isBlank() || outboxKey.length()<32 || outboxKey.equals(jwtKey)) throw new IllegalStateException("Production requires configured delivery and a separate outbox encryption key");
        if(!cookie || origins.isEmpty() || origins.stream().anyMatch(v->!v.startsWith("https://") || v.contains("*"))) throw new IllegalStateException("Production requires Secure cookies and explicit HTTPS origins");
        if(admin) throw new IllegalStateException("Production administrative access remains disabled until MFA is integrated");
        if(legacyGrace.startsWith("9999-")) throw new IllegalStateException("Production requires an explicit legacy notification and grace-period rollout deadline");
        if(!PasswordPolicy.hasCompromisedCorpus()) throw new IllegalStateException("Production requires the offline compromised password hash corpus");
    }
}
