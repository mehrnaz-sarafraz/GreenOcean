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
    private final URI gateway;private final String authorization;private final ObjectMapper json;
    private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build();
    public WebhookMailTransport(@Value("${greenocean.mail.gateway-url:}") String gateway,@Value("${greenocean.mail.gateway-token:}") String token,ObjectMapper json) {
        this.gateway=gateway.isBlank()?null:URI.create(gateway);this.authorization=token;this.json=json;
        if(this.gateway!=null && (!"https".equals(this.gateway.getScheme()) || this.gateway.getUserInfo()!=null || this.gateway.getFragment()!=null || token.isBlank()))
            throw new IllegalStateException("Mail gateway requires HTTPS and a private authorization token");
    }
    public void send(UUID deliveryId,String recipient,String template,String secret) {
        if(gateway==null) throw new IllegalStateException("Mail delivery is not configured");
        try {
            var body=json.writeValueAsString(Map.of("deliveryId",deliveryId.toString(),"recipient",recipient,"template",template,"secret",secret));
            var request=HttpRequest.newBuilder(gateway).timeout(Duration.ofSeconds(10)).header("Authorization","Bearer "+authorization)
              .header("Content-Type","application/json").header("Idempotency-Key",deliveryId.toString()).POST(HttpRequest.BodyPublishers.ofString(body)).build();
            var response=client.send(request,HttpResponse.BodyHandlers.discarding());
            if(response.statusCode()<200 || response.statusCode()>=300) throw new IllegalStateException("Mail gateway did not accept delivery");
        } catch(InterruptedException e) {Thread.currentThread().interrupt();throw new IllegalStateException("Delivery interrupted");}
        catch(Exception e) {throw new IllegalStateException("Mail gateway delivery failed");}
    }
}
