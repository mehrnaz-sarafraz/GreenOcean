package com.greenocean.backend.trust;

import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import com.greenocean.backend.common.exception.ForbiddenException;
import java.util.*;

/** Extra restoration restrictions never replace source-domain authorization. */
@Component
public class RecoveryBoundary implements HandlerInterceptor,WebMvcConfigurer {
    private final RecoveryReplay replay;
    public RecoveryBoundary(RecoveryReplay replay){this.replay=replay;}
    public void addInterceptors(InterceptorRegistry registry){registry.addInterceptor(this).addPathPatterns("/api/**");}
    static String domain(String path){
        if(path.startsWith("/api/v1/clinical")||path.startsWith("/api/v1/professional")||
            path.startsWith("/api/v1/admin/professional-")||path.startsWith("/api/v1/clinic-integration/followups"))return "CLINIC";
        if(path.startsWith("/api/v1/clinic"))return "FINANCE";
        if(path.startsWith("/api/v1/wellbeing"))return "WELLBEING";
        if(path.startsWith("/api/v1/listener")||path.startsWith("/api/v1/live-support"))return "LISTENER";
        return "SOCIAL";
    }
    private boolean obligation(String path){
        // These existing operations retain all original actor/purpose/revision checks.
        return path.endsWith("/end")||path.endsWith("/revoke")||path.endsWith("/refund")||
            path.endsWith("/reconcile-refund")||path.endsWith("/reconcile")||path.endsWith("/transition")||
            path.endsWith("/consent")||path.contains("/disputes")||path.contains("/rights")||
            path.endsWith("/reports")||path.equals("/api/v1/moderation-appeals")||
            path.startsWith("/api/v1/trust/")||path.startsWith("/api/v1/operations/")||
            path.startsWith("/api/v1/blocks/");
    }
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler){
        if(!replay.configured())return true;
        String path=request.getRequestURI();
        if(path.equals("/api/v1/health")||path.startsWith("/api/v1/recovery/")||
            path.startsWith("/api/v1/auth/")||path.equals("/api/v1/crisis-resources"))return true;
        if(!replay.ready())throw new ForbiddenException("Restored services await independent privacy replay verification");
        String domain=domain(path);
        boolean clinicalCommerce=path.startsWith("/api/v1/clinic/");
        var authentication=SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof JwtAuthenticationToken jwt){
            UUID subject=UUID.fromString(jwt.getToken().getSubject());
            if(replay.restricted(subject,domain)||(clinicalCommerce&&replay.restricted(subject,"CLINIC")))
                throw new ForbiddenException("Restored subject data remain restricted by the verified privacy manifest");
        }
        boolean paused=replay.stopped(domain)||(clinicalCommerce&&replay.stopped("CLINIC"));
        if(paused&&Set.of("POST","PUT","PATCH").contains(request.getMethod())&&!obligation(path))
            throw new ForbiddenException("This restored service remains paused pending separate runtime authorization");
        return true;
    }
}
