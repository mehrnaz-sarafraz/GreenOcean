package com.greenocean.backend.preference.controller;

import com.greenocean.backend.preference.dto.PublishedPolicyResponse;
import com.greenocean.backend.preference.service.PolicyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {
    private final PolicyService service;

    public PolicyController(PolicyService service) {
        this.service = service;
    }

    @GetMapping("/{key}")
    public PublishedPolicyResponse published(@PathVariable String key) {
        return service.published(key);
    }
}
