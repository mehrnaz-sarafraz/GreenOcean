package com.greenocean.backend.trust;
import jakarta.servlet.*;import jakarta.servlet.http.*;import org.springframework.web.filter.OncePerRequestFilter;import org.springframework.stereotype.Component;import java.io.IOException;
@Component @org.springframework.core.annotation.Order(-300)
public class SafeTelemetryFilter extends OncePerRequestFilter {
 private final SafeTelemetry telemetry;public SafeTelemetryFilter(SafeTelemetry telemetry){this.telemetry=telemetry;}
 @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{long started=System.nanoTime();boolean failed=true;try{chain.doFilter(request,response);failed=false;}finally{telemetry.observe(SafeTelemetry.service(request.getMethod(),request.getRequestURI()),failed?500:response.getStatus(),System.nanoTime()-started);}}
}
