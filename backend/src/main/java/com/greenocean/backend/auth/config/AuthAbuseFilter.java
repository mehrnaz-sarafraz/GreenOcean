package com.greenocean.backend.auth.config;
import com.greenocean.backend.auth.service.AuthRateLimit;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;

@Component
public class AuthAbuseFilter extends OncePerRequestFilter {
    private final AuthRateLimit limits;
    public AuthAbuseFilter(AuthRateLimit limits) {this.limits=limits;}
    protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
        String path=req.getRequestURI();
        res.setHeader("Cache-Control","no-store");
        if("POST".equals(req.getMethod()) && (path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/account-assistance/")) &&
           !limits.allow("ip:"+req.getRemoteAddr()+":"+path,30,Duration.ofMinutes(1))) {
            res.setStatus(429);res.setHeader("Retry-After","60");res.setContentType("application/json");
            res.getWriter().write("{\"status\":429,\"message\":\"Too many attempts. Please wait before trying again.\"}");return;
        }
        chain.doFilter(req,res);
    }
}
