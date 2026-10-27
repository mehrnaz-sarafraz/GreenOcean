package com.greenocean.backend.assistance.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import tools.jackson.databind.ObjectMapper;

/** Trusted mail gateway; it must deliver idempotently by deliveryId. No client-supplied destinations. */
@Component
public class WebhookMailTransport implements MailTransport {
    @Value("${greenocean.auth.recovery-base-url:}") private String recoveryBase="";
    private final URI gateway;private final String authorization;private final ObjectMapper json;
    private final HttpClient client;
    private final String sender,mode;
    private final Set<String> authorizedRecipients;
    @org.springframework.beans.factory.annotation.Autowired
    public WebhookMailTransport(@Value("${greenocean.mail.gateway-url:}") String gateway,@Value("${greenocean.mail.gateway-token:}") String token,ObjectMapper json,
      @Value("${greenocean.mail.from:no-reply@greenocean.invalid}") String sender,@Value("${greenocean.mail.mode:development}") String mode,
      @Value("${greenocean.mail.staging-recipients:}") String recipients) {
        this(gateway,token,json,sender,mode,recipients,HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build());
    }
    public WebhookMailTransport(String gateway,String token,ObjectMapper json,String sender,String mode,String recipients,HttpClient client) {
        this.gateway=gateway.isBlank()?null:URI.create(gateway);this.authorization=token;this.json=json;
        this.client=client;this.sender=sender;this.mode=mode;
        this.authorizedRecipients=new HashSet<>();for(String address:recipients.split(","))if(!address.isBlank())authorizedRecipients.add(address.trim().toLowerCase(Locale.ROOT));
        if(this.gateway!=null && (!"https".equals(this.gateway.getScheme()) || this.gateway.getUserInfo()!=null || this.gateway.getFragment()!=null || token.isBlank()))
            throw new IllegalStateException("Mail gateway requires HTTPS and a private authorization token");
    }
    public void send(UUID deliveryId,String recipient,String template,String secret) {
        submit(deliveryId,sender,recipient,template,secret,"en");
    }
    @Override public Receipt submit(UUID deliveryId,String sender,String recipient,String template,String secret,String language) {
        return submit(deliveryId,sender,recipient,template,secret,language,java.time.Instant.now().plusSeconds(900));
    }
    @Override public Receipt submit(UUID deliveryId,String sender,String recipient,String template,String secret,String language,java.time.Instant expiresAt) {
        if(gateway==null) throw new MailDeliveryFailure(MailDeliveryFailure.Kind.RETRYABLE);
        if(!expiresAt.isAfter(java.time.Instant.now()))throw new MailDeliveryFailure(MailDeliveryFailure.Kind.PERMANENT);
        if(!Set.of("email-verification","password-reset","security-notification").contains(template) ||
           !sender.matches(com.greenocean.backend.auth.service.EmailIdentity.PATTERN) ||
           ("staging".equals(mode)&&!authorizedRecipients.contains(recipient.toLowerCase(Locale.ROOT))))
            throw new MailDeliveryFailure(MailDeliveryFailure.Kind.PERMANENT);
        try {
            var envelope=new java.util.HashMap<String,Object>(Map.of("deliveryId",deliveryId.toString(),"sender",sender,"recipient",recipient,"template",template,"templateVersion",1,"language",Set.of("en","fa").contains(language)?language:"en","expiresAt",expiresAt.toString(),"secret",secret));
            if(template.equals("password-reset")&&!recoveryBase.isBlank()){var base=URI.create(recoveryBase);if(!"https".equals(base.getScheme())||base.getHost()==null||base.getUserInfo()!=null||base.getQuery()!=null||base.getFragment()!=null)throw new MailDeliveryFailure(MailDeliveryFailure.Kind.PERMANENT);envelope.put("recoveryUrl",recoveryBase+"#token="+secret);}
            var body=json.writeValueAsString(envelope);
            var request=HttpRequest.newBuilder(gateway).timeout(Duration.ofSeconds(10)).header("Authorization","Bearer "+authorization)
              .header("Content-Type","application/json").header("Idempotency-Key",deliveryId.toString()).POST(HttpRequest.BodyPublishers.ofString(body)).build();
            var response=client.send(request,HttpResponse.BodyHandlers.discarding());
            int status=response.statusCode();
            if(status>=200&&status<300) {
                String reference=response.headers().firstValue("X-Provider-Reference").orElse(null);
                if(reference!=null&&!reference.matches("[A-Za-z0-9_.:-]{1,160}"))reference=null;
                return new Receipt(reference);
            }
            throw new MailDeliveryFailure(status==408||status==409?MailDeliveryFailure.Kind.UNCERTAIN:status==429||status>=500?MailDeliveryFailure.Kind.RETRYABLE:MailDeliveryFailure.Kind.PERMANENT);
        } catch(MailDeliveryFailure e){throw e;}
        catch(InterruptedException e) {Thread.currentThread().interrupt();throw new MailDeliveryFailure(MailDeliveryFailure.Kind.UNCERTAIN);}
        catch(Exception e) {throw new MailDeliveryFailure(MailDeliveryFailure.Kind.UNCERTAIN);}
    }
}
