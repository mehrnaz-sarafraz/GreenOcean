package com.greenocean.backend.preference.service;

import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.preference.dto.PublishedPolicyResponse;
import com.greenocean.backend.preference.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyService {
    private final PolicyRepository repository;

    public PolicyService(PolicyRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PublishedPolicyResponse published(String key) {
        String normalized = key.trim().toLowerCase(java.util.Locale.ROOT);
        if(normalized.length()>80 || !normalized.matches("[a-z0-9]+(?:-[a-z0-9]+)*"))
            throw new IllegalArgumentException("Invalid policy key");
        return repository.latestPublished(normalized)
                .orElseThrow(() -> new NotFoundException("Published policy was not found"));
    }
}
