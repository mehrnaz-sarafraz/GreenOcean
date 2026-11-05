package com.greenocean.backend.trust;
import org.springframework.web.bind.annotation.*;
@RestController
public class PublicPilotController {
    private final PilotReadiness readiness;
    public PublicPilotController(PilotReadiness readiness){this.readiness=readiness;}
    @GetMapping("/api/v1/public/pilot-readiness")public Object status(@RequestParam(defaultValue="IR")String country){return readiness.publicMatrix(country);}
}
