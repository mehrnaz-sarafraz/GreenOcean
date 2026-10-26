package com.greenocean.backend.identity;

import com.greenocean.backend.auth.service.AuthRateLimit;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;

/** Shared durable budget: changing locale, query, or public route does not reset the window. */
@Component
public class PublicReadAbuseFilter extends OncePerRequestFilter {
    private final AuthRateLimit limits;
    private final int maximum;
    public PublicReadAbuseFilter(AuthRateLimit limits,@Value("${greenocean.security.public-read-limit:120}") int maximum) {
        if(maximum<1)throw new IllegalArgumentException("Public read limit must be positive");
        this.limits=limits;this.maximum=maximum;
    }
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        String path=request.getRequestURI();
        boolean publicRead=path.startsWith("/api/v1/public/")||path.startsWith("/api/v1/policies/")||path.startsWith("/api/v1/geography/")||path.equals("/api/v1/crisis-resources");
        if("GET".equals(request.getMethod())&&publicRead&&!limits.allow("public-read:"+request.getRemoteAddr(),maximum,Duration.ofMinutes(1))) {
            response.setStatus(429);response.setHeader("Retry-After","60");response.setHeader("Cache-Control","no-store");response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"message\":\"Too many public requests. Please wait and try again.\"}");return;
        }
        chain.doFilter(request,response);
    }
}
