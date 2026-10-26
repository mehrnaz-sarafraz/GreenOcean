package com.greenocean.backend.auth.controller;
import com.greenocean.backend.auth.dto.*;
import com.greenocean.backend.auth.service.AuthenticationService;
import com.greenocean.backend.common.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.CorsConfigurationSource;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth/web")
public class WebSessionController {
    private final AuthenticationService auth;private final CorsConfigurationSource cors;private final boolean secure;
    public WebSessionController(AuthenticationService auth,@org.springframework.beans.factory.annotation.Qualifier("corsConfigurationSource") CorsConfigurationSource cors,@Value("${greenocean.auth.web-cookie-secure:true}") boolean secure) {this.auth=auth;this.cors=cors;this.secure=secure;}
    private void csrf(HttpServletRequest request) {
        String origin=request.getHeader("Origin");var config=cors.getCorsConfiguration(request);
        if(!"1".equals(request.getHeader("X-GreenOcean-CSRF")) || origin==null || config==null || config.checkOrigin(origin)==null)
            throw new ForbiddenException("Invalid browser session request");
    }
    @PostMapping("/login") public ResponseEntity<TokenResponse> login(HttpServletRequest request,@Valid @RequestBody LoginRequest input) {
        csrf(request);return response(auth.login(input));
    }
    @PostMapping("/refresh") public ResponseEntity<TokenResponse> refresh(HttpServletRequest request,@CookieValue(name="greenocean_refresh",defaultValue="") String token) {
        csrf(request);return response(auth.refresh(token));
    }
    @PostMapping("/logout") public ResponseEntity<Void> logout(HttpServletRequest request,@CookieValue(name="greenocean_refresh",defaultValue="") String token) {
        csrf(request);auth.logout(token);return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE,cookie("",Duration.ZERO)).build();
    }
    private String cookie(String token,Duration age) {
        return ResponseCookie.from("greenocean_refresh",token).httpOnly(true).secure(secure).sameSite("Strict").path("/api/v1/auth/web").maxAge(age).build().toString();
    }
    private ResponseEntity<TokenResponse> response(TokenResponse tokens) {
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookie(tokens.refreshToken(),Duration.ofDays(30)))
          .header(HttpHeaders.CACHE_CONTROL,"no-store").body(new TokenResponse(tokens.accessToken(),"cookie",tokens.tokenType(),tokens.expiresIn()));
    }
}
