package com.greenocean.backend.auth.config;
import com.greenocean.backend.auth.service.AuthRateLimit;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;

@Component
public class AuthAbuseFilter extends OncePerRequestFilter {
    private final AuthRateLimit limits;
    private final int registrationAttempts;
    private final int registrationWindow;
    public AuthAbuseFilter(AuthRateLimit limits,
       @Value("${greenocean.auth.registration-attempts:30}") int attempts,
       @Value("${greenocean.auth.registration-window-seconds:60}") int seconds) {
        this.limits=limits;
        if(attempts<1 || seconds<1) throw new IllegalArgumentException("Registration rate limits must be positive");
        this.registrationAttempts=attempts;this.registrationWindow=seconds;
    }
    protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
        String path=req.getRequestURI();
        boolean registration=path.equals("/api/v1/auth/register");
        res.setHeader("Cache-Control","no-store");res.setHeader("Referrer-Policy","no-referrer");
        if("POST".equals(req.getMethod()) && (path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/account-assistance/")) &&
           !limits.allow("ip:"+req.getRemoteAddr()+":"+path,registration?registrationAttempts:30,Duration.ofSeconds(registration?registrationWindow:60))) {
            res.setStatus(429);res.setHeader("Retry-After",String.valueOf(registration?registrationWindow:60));res.setContentType("application/json");
            res.getWriter().write("{\"status\":429,\"message\":\"Too many attempts. Please wait before trying again.\"}");return;
        }
        if((path.startsWith("/api/v1/auth/")||path.startsWith("/api/v1/account-assistance/")) && "POST".equals(req.getMethod())) {
            // Bound bytes before JSON parsing/hashing, including requests with no Content-Length.
            byte[] body=req.getInputStream().readNBytes(8193);
            if(body.length>8192) {
                res.setStatus(413);res.setContentType("application/json");
                res.getWriter().write("{\"status\":413,\"message\":\"Registration request is too large.\"}");return;
            }
            var input=new java.io.ByteArrayInputStream(body);
            chain.doFilter(new HttpServletRequestWrapper(req) {
                @Override public ServletInputStream getInputStream() {
                    return new ServletInputStream() {
                        public int read() {return input.read();}
                        public boolean isFinished() {return input.available()==0;}
                        public boolean isReady() {return true;}
                        public void setReadListener(ReadListener listener) {throw new UnsupportedOperationException("Synchronous JSON request");}
                    };
                }
                @Override public java.io.BufferedReader getReader() {
                    return new java.io.BufferedReader(new java.io.InputStreamReader(getInputStream(),java.nio.charset.StandardCharsets.UTF_8));
                }
            },res);return;
        }
        chain.doFilter(req,res);
    }
}
