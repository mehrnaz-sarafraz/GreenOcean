package com.greenocean.backend.trust;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;

/** Process-lifetime, low-cardinality aggregates. No IDs, headers, query strings or bodies. */
@Component
public class SafeTelemetry {
 public enum Service {AUTH,SOCIAL,GROUP,LISTENER,WELLBEING,WELLBEING_WRITE,ACCOUNT_EXPORT,ACCOUNT_ERASURE,EDUCATION,CLINIC,TRUST,HEALTH,OTHER}
 private final boolean enabled;private final LongAdder[][] counts=new LongAdder[Service.values().length][5],nanos=new LongAdder[Service.values().length][5];
 public SafeTelemetry(@Value("${greenocean.telemetry.enabled:false}")boolean enabled){this.enabled=enabled;for(int i=0;i<counts.length;i++)for(int j=0;j<5;j++){counts[i][j]=new LongAdder();nanos[i][j]=new LongAdder();}}
 public static Service service(String path){if(path.equals("/api/v1/health"))return Service.HEALTH;if(path.startsWith("/api/v1/auth/"))return Service.AUTH;if(path.startsWith("/api/v1/posts")||path.contains("social-moderation"))return Service.SOCIAL;if(path.startsWith("/api/v1/communities")||path.startsWith("/api/v1/community-"))return Service.GROUP;if(path.startsWith("/api/v1/listener")||path.startsWith("/api/v1/live-support"))return Service.LISTENER;if(path.startsWith("/api/v1/wellbeing"))return Service.WELLBEING;if(path.startsWith("/api/v1/growth")||path.startsWith("/api/v1/learning"))return Service.EDUCATION;if(path.startsWith("/api/v1/clinic")||path.startsWith("/api/v1/clinical"))return Service.CLINIC;if(path.startsWith("/api/v1/trust"))return Service.TRUST;return Service.OTHER;}
 public void observe(Service service,int status,long elapsedNanos){if(!enabled)return;int band=Math.max(0,Math.min(4,status/100-1));counts[service.ordinal()][band].increment();nanos[service.ordinal()][band].add(Math.max(0,elapsedNanos));}
 public static Service service(String method,String path){if("GET".equals(method)&&path.equals("/api/v1/account/archive"))return Service.ACCOUNT_EXPORT;if("DELETE".equals(method)&&path.equals("/api/v1/account"))return Service.ACCOUNT_ERASURE;if(path.startsWith("/api/v1/wellbeing")&&Set.of("POST","PUT","PATCH","DELETE").contains(method))return Service.WELLBEING_WRITE;if(path.startsWith("/api/v1/operations"))return Service.TRUST;return service(path);}
 public Map<String,Object> snapshot(){var rows=new ArrayList<Map<String,Object>>();for(Service s:Service.values())for(int j=0;j<5;j++){long count=counts[s.ordinal()][j].sum();if(count>0)rows.add(Map.of("service",s.name(),"statusFamily",(j+1)+"xx","requests",count,"elapsedNanos",nanos[s.ordinal()][j].sum()));}return Map.of("enabled",enabled,"retention","PROCESS_LIFETIME_NO_EXTERNAL_TRANSFER","dimensions",List.of("fixedService","statusFamily"),"rows",rows);}
}
