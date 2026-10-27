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
    private final AuthenticationService auth;private final CorsConfigurationSource cors;private final boolean secure;private final com.greenocean.backend.auth.service.PrivilegedWebAuthn mfa;private final com.greenocean.backend.auth.service.RecentAuthentication recent;
    public WebSessionController(AuthenticationService auth,@org.springframework.beans.factory.annotation.Qualifier("corsConfigurationSource") CorsConfigurationSource cors,@Value("${greenocean.auth.web-cookie-secure:true}") boolean secure,com.greenocean.backend.auth.service.PrivilegedWebAuthn mfa,com.greenocean.backend.auth.service.RecentAuthentication recent) {this.auth=auth;this.cors=cors;this.secure=secure;this.mfa=mfa;this.recent=recent;}
    private void csrf(HttpServletRequest request) {
        String origin=request.getHeader("Origin");var config=cors.getCorsConfiguration(request);
        if(!"1".equals(request.getHeader("X-GreenOcean-CSRF")) || origin==null || config==null || config.checkOrigin(origin)==null)
            throw new ForbiddenException("Invalid browser session request");
    }
    @PostMapping("/login") public ResponseEntity<TokenResponse> login(HttpServletRequest request,@Valid @RequestBody LoginRequest input) {
        csrf(request);return response(auth.login(input,"WEB"));
    }
    @PostMapping("/refresh") public ResponseEntity<TokenResponse> refresh(HttpServletRequest request,@CookieValue(name="greenocean_refresh",defaultValue="") String token) {
        csrf(request);return response(auth.refresh(token));
    }
    @PostMapping("/logout") public ResponseEntity<Void> logout(HttpServletRequest request,@CookieValue(name="greenocean_refresh",defaultValue="") String token) {
        csrf(request);auth.logout(token);return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE,cookie("",Duration.ZERO)).build();
    }
    @PostMapping("/reauthenticate") public ResponseEntity<TokenResponse> reauthenticate(HttpServletRequest request,@org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt,@CookieValue(name="greenocean_refresh",defaultValue="") String token,@Valid @RequestBody SessionSecurityController.Proof proof){
        csrf(request);if(jwt==null)throw new ForbiddenException("Sign in again to confirm your password");
        return response(recent.completePassword(java.util.UUID.fromString(jwt.getSubject()),java.util.UUID.fromString(jwt.getClaimAsString("sid")),proof.password(),token));
    }
    @PostMapping("/mfa/authentication/verify") public ResponseEntity<TokenResponse> mfa(HttpServletRequest request,@org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt,@CookieValue(name="greenocean_refresh",defaultValue="") String token,@Valid @RequestBody PrivilegedMfaController.Finish input){
        csrf(request);if(jwt==null)throw new ForbiddenException("Sign in again to verify the security key");
        return response(mfa.completeAuthentication(java.util.UUID.fromString(jwt.getSubject()),java.util.UUID.fromString(jwt.getClaimAsString("sid")),input.ceremonyId(),input.credentialJson(),token));
    }
    private String cookie(String token,Duration age) {
        return ResponseCookie.from("greenocean_refresh",token).httpOnly(true).secure(secure).sameSite("Strict").path("/api/v1/auth/web").maxAge(age).build().toString();
    }
    private ResponseEntity<TokenResponse> response(TokenResponse tokens) {
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookie(tokens.refreshToken(),Duration.ofHours(24)))
          .header(HttpHeaders.CACHE_CONTROL,"no-store").body(new TokenResponse(tokens.accessToken(),"cookie",tokens.tokenType(),tokens.expiresIn()));
    }
}
