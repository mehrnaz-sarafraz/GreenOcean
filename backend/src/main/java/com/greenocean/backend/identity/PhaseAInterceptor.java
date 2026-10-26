package com.greenocean.backend.identity;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.UUID;

@Component
public class PhaseAInterceptor implements HandlerInterceptor {
    private final AccessPolicy access;
    private final boolean adminEnabled;private final com.greenocean.backend.auth.service.RecentAuthentication recent;private final com.greenocean.backend.auth.service.SecurityEvents events;
    public PhaseAInterceptor(AccessPolicy access,@Value("${greenocean.security.admin-enabled:false}") boolean adminEnabled,com.greenocean.backend.auth.service.RecentAuthentication recent,com.greenocean.backend.auth.service.SecurityEvents events) {
        this.access=access; this.adminEnabled=adminEnabled;this.recent=recent;this.events=events;
    }
    @Override public boolean preHandle(HttpServletRequest req,HttpServletResponse res,Object handler) {
        String path=req.getRequestURI();
        res.setHeader("Cache-Control","no-store");
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwt)) return true;
        if(path.startsWith("/api/v1/admin")){
            var user=UUID.fromString(jwt.getToken().getSubject());var session=UUID.fromString(jwt.getToken().getClaimAsString("sid"));
            if(!adminEnabled){events.record(user,session,com.greenocean.backend.auth.service.SecurityEvents.Event.PRIVILEGED_DENIED);throw new com.greenocean.backend.common.exception.ForbiddenException("Administrative access is disabled until strong authentication is configured");}
            recent.require(user,session,true);
        }
        if (!path.startsWith("/api/v1/") || path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/identity") ||
            path.startsWith("/api/v1/profiles/me") || path.startsWith("/api/v1/preferences/me") || path.startsWith("/api/v1/account") ||
            path.startsWith("/api/v1/public/") || path.startsWith("/api/v1/policies/") || path.equals("/api/v1/crisis-resources") ||
            path.startsWith("/api/v1/geography/")) return true;
        String service=path.contains("consultation") || path.contains("/conversations/professionals/") ? "PROFESSIONAL_CARE" :
            path.contains("listener") ? "LISTENER" : path.startsWith("/api/v1/professionals") ? "PROFESSIONAL_DIRECTORY" : "COMMUNITY";
        access.require(UUID.fromString(jwt.getToken().getSubject()),service);
        if("POST".equals(req.getMethod()) && (path.equals("/api/v1/posts") || path.contains("/comments")))
            access.requireDisplayReview(UUID.fromString(jwt.getToken().getSubject()));
        return true;
    }
}
