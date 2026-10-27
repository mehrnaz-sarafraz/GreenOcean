package com.greenocean.backend.assistance.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component @Profile({"prod","staging"})
public class MailDeploymentValidator {
    public MailDeploymentValidator(org.springframework.core.env.Environment environment,
      @Value("${greenocean.mail.mode:development}") String mode,@Value("${greenocean.mail.from:no-reply@greenocean.invalid}") String sender,
      @Value("${greenocean.mail.enabled:false}") boolean enabled,@Value("${greenocean.mail.gateway-url:}") String gateway,
      @Value("${greenocean.mail.gateway-token:}") String token,@Value("${greenocean.mail.staging-recipients:}") String recipients,
      @Value("${greenocean.mail.templates-approved:false}") boolean templatesApproved,@Value("${greenocean.auth.recovery-base-url:}") String recoveryBase,
      @Value("${greenocean.mail.outbox-key:}") String outboxKey,@Value("${greenocean.auth.jwt-secret}") String jwtKey) {
        java.net.URI recovery;try{recovery=java.net.URI.create(recoveryBase);}catch(Exception e){throw new IllegalStateException("An approved HTTPS recovery destination is required");}
        if(!"https".equals(recovery.getScheme())||recovery.getHost()==null||recovery.getUserInfo()!=null||recovery.getQuery()!=null||recovery.getFragment()!=null||outboxKey.length()<32||outboxKey.equals(jwtKey))throw new IllegalStateException("An approved HTTPS recovery destination and separate stable delivery key are required");
        boolean production=environment.matchesProfiles("prod");
        if(!enabled||!gateway.startsWith("https://")||token.isBlank()||!templatesApproved||
           !sender.matches(com.greenocean.backend.auth.service.EmailIdentity.PATTERN)||sender.endsWith(".invalid")||
           !(production?"production":"staging").equals(mode))throw new IllegalStateException("Configured delivery, reviewed templates, sender and explicit mail mode are required");
        if(!production&&recipients.isBlank())throw new IllegalStateException("Staging requires explicitly authorized recipient mailboxes");
    }
}
