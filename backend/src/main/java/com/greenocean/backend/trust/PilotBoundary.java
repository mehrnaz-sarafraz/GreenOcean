package com.greenocean.backend.trust;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.*;
@Component
public class PilotBoundary implements HandlerInterceptor,WebMvcConfigurer {
    private final PilotGate gate;public PilotBoundary(PilotGate gate){this.gate=gate;}
    public void addInterceptors(InterceptorRegistry registry){registry.addInterceptor(this).addPathPatterns("/api/**");}
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler){
        if(!gate.enforced())return true;String path=request.getRequestURI(),method=request.getMethod();
        if(path.startsWith("/api/v1/auth/")||path.startsWith("/api/v1/public/")||path.startsWith("/api/v1/pilot/")||path.startsWith("/api/v1/account")||path.startsWith("/api/v1/trust/")||path.startsWith("/api/v1/operations/")||path.startsWith("/api/v1/recovery/")||path.startsWith("/api/v1/admin/")||path.startsWith("/api/v1/policies/")||path.equals("/api/v1/health")||path.equals("/api/v1/crisis-resources")||path.equals("/api/v1/reports")||path.endsWith("/reports")||method.equals("DELETE"))return true;
        var auth=SecurityContextHolder.getContext().getAuthentication();if(!(auth instanceof JwtAuthenticationToken jwt))return true;
        UUID who=UUID.fromString(jwt.getToken().getSubject());
        if(path.startsWith("/api/v1/profiles/")&&!path.equals("/api/v1/profiles/me"))gate.require(who,"COMMUNITY");
        else if(path.startsWith("/api/v1/posts")||path.startsWith("/api/v1/communities"))gate.require(who,"COMMUNITY");
        else if(path.startsWith("/api/v1/growth-library")&&!path.startsWith("/api/v1/growth-library/editorial")&&Set.of("POST","PUT","PATCH").contains(method)){gate.require(who,"WELLBEING_EDUCATION");if(path.contains("/posts/"))gate.require(who,"COMMUNITY");}
        else if(path.startsWith("/api/v1/wellbeing")&&Set.of("POST","PUT","PATCH").contains(method))gate.require(who,"WELLBEING_EDUCATION");
        // Listener/clinical admission is checked inside source services, preserving current sessions,
        // record access,refunds,cancellations and account rights after opt-out.
        return true;
    }
}
