package com.greenocean.backend.trust;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/public/service-status")
public class ServiceStatusController {
 private final OperationsCommunications communications;
 public ServiceStatusController(OperationsCommunications communications){this.communications=communications;}
 @GetMapping public Object status(@RequestParam(defaultValue="en")String locale){return communications.publicStatus(locale);}
}
