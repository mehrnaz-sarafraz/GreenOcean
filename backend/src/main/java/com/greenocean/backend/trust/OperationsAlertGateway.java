package com.greenocean.backend.trust;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.net.URI;import java.net.http.*;import java.time.Duration;import javax.crypto.Mac;import javax.crypto.spec.SecretKeySpec;import java.nio.charset.StandardCharsets;import java.util.*;

/** Trusted deployment configuration only. No redirect, response-body retention or caller-supplied URL. */
@Component
public class OperationsAlertGateway {
 private final Map<String,String> endpoints;private final String secret;private final boolean testHttp;
 private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).followRedirects(HttpClient.Redirect.NEVER).version(HttpClient.Version.HTTP_1_1).build();
 public OperationsAlertGateway(@Value("${greenocean.operations.alert-primary-url:}")String primary,@Value("${greenocean.operations.alert-escalation-url:}")String escalation,@Value("${greenocean.operations.alert-signing-key:}")String secret,Environment environment){endpoints=Map.of("PRIMARY",primary,"ESCALATION",escalation);this.secret=secret;testHttp=environment.matchesProfiles("test");}
 public boolean configured(String key){try{uri(key);return secret.getBytes(StandardCharsets.UTF_8).length>=32;}catch(RuntimeException invalid){return false;}}
 private URI uri(String key){URI u=URI.create(endpoints.getOrDefault(key,""));if(u.getHost()==null||u.getUserInfo()!=null||u.getFragment()!=null||u.getQuery()!=null||!("https".equals(u.getScheme())||(testHttp&&"http".equals(u.getScheme())&&Set.of("127.0.0.1","localhost").contains(u.getHost()))))throw new IllegalArgumentException("An approved HTTPS alert destination is required");return u;}
 public int send(String key,UUID delivery,String payload){if(!configured(key))return 0;try{byte[] body=payload.getBytes(StandardCharsets.UTF_8);var mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));String signature=HexFormat.of().formatHex(mac.doFinal(body));var request=HttpRequest.newBuilder(uri(key)).timeout(Duration.ofSeconds(4)).header("Content-Type","application/json").header("X-GreenOcean-Delivery",delivery.toString()).header("X-GreenOcean-Signature",signature).POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();return http.send(request,HttpResponse.BodyHandlers.discarding()).statusCode();}catch(InterruptedException interrupted){Thread.currentThread().interrupt();return 0;}catch(Exception unavailable){return 0;}}
 @jakarta.annotation.PreDestroy public void close(){http.close();}
}
