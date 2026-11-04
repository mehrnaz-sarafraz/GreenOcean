package com.greenocean.backend.trust;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.jwt.Jwt;import org.springframework.transaction.annotation.Transactional;import java.util.UUID;
@RestController
public class SafeTelemetryController {
 private final SafeTelemetry telemetry;private final TrustAuthority authority;public SafeTelemetryController(SafeTelemetry telemetry,TrustAuthority authority){this.telemetry=telemetry;this.authority=authority;}
 @GetMapping("/api/v1/trust/metrics") @Transactional public Object metrics(@AuthenticationPrincipal Jwt j){authority.require(UUID.fromString(j.getSubject()),UUID.fromString(j.getClaimAsString("sid")),"PLATFORM");return telemetry.snapshot();}
}
