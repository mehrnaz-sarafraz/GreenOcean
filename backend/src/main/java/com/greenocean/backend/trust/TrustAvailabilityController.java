package com.greenocean.backend.trust;
import org.springframework.web.bind.annotation.*;
@RestController
public class TrustAvailabilityController {
 private final TrustCoverage coverage;public TrustAvailabilityController(TrustCoverage coverage){this.coverage=coverage;}
 @GetMapping("/api/v1/public/safety-availability")public Object availability(@RequestParam String domain){return coverage.availability(domain);}
}
