package com.greenocean.backend.auth.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.*;
import java.util.Set;
@Component
public record SessionPolicy(Duration webOverall,Duration webIdle,Duration mobileOverall,Duration mobileIdle,Duration privilegedOverall,Duration privilegedIdle,Duration recent) {
 public SessionPolicy(@Value("${greenocean.session.web-overall:PT24H}") Duration webOverall,@Value("${greenocean.session.web-idle:PT30M}") Duration webIdle,
 @Value("${greenocean.session.mobile-overall:P30D}") Duration mobileOverall,@Value("${greenocean.session.mobile-idle:PT24H}") Duration mobileIdle,
 @Value("${greenocean.session.privileged-overall:PT12H}") Duration privilegedOverall,@Value("${greenocean.session.privileged-idle:PT30M}") Duration privilegedIdle,
 @Value("${greenocean.session.recent-auth:PT10M}") Duration recent){
  for(Duration value:new Duration[]{webOverall,webIdle,mobileOverall,mobileIdle,privilegedOverall,privilegedIdle,recent})if(value.isNegative()||value.isZero())throw new IllegalArgumentException("Session durations must be positive");
  if(webIdle.compareTo(webOverall)>0||mobileIdle.compareTo(mobileOverall)>0||privilegedIdle.compareTo(privilegedOverall)>0)throw new IllegalArgumentException("Idle timeout cannot exceed overall timeout");
  this.webOverall=webOverall;this.webIdle=webIdle;this.mobileOverall=mobileOverall;this.mobileIdle=mobileIdle;this.privilegedOverall=privilegedOverall;this.privilegedIdle=privilegedIdle;this.recent=recent;
 }
 public Duration overall(String client,String scope){return "PENDING".equals(scope)?Duration.ofHours(1):"PRIVILEGED".equals(scope)?privilegedOverall:"MOBILE".equals(client)?mobileOverall:webOverall;}
 public Duration idle(String client,String scope){return "PENDING".equals(scope)?Duration.ofMinutes(15):"PRIVILEGED".equals(scope)?privilegedIdle:"MOBILE".equals(client)?mobileIdle:webIdle;}
 public static Instant earlier(Instant a,Instant b){return a.isBefore(b)?a:b;}
 public static boolean privileged(java.util.Collection<String> roles){return roles.stream().anyMatch(Set.of("ADMIN","MODERATOR")::contains);}
}
