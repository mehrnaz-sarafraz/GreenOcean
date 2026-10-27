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
    @Transactional(readOnly=true)
    public java.util.List<PublishedPolicyResponse> history(String key,String locale) {
        if(!key.matches("[a-z0-9]+(?:-[a-z0-9]+)*")||key.length()>80||!java.util.Set.of("en","fa").contains(locale))throw new IllegalArgumentException("Invalid policy key or locale");
        return repository.history(key,locale);
    }

    @Transactional(readOnly = true)
    public PublishedPolicyResponse published(String key) {
        return published(key,"en");
    }
    public PublishedPolicyResponse published(String key,String locale) {
        if(!java.util.Set.of("en","fa").contains(locale))throw new IllegalArgumentException("Unsupported policy locale");
        String normalized = key.trim().toLowerCase(java.util.Locale.ROOT);
        if(normalized.length()>80 || !normalized.matches("[a-z0-9]+(?:-[a-z0-9]+)*"))
            throw new IllegalArgumentException("Invalid policy key");
        return repository.latestPublished(normalized,locale)
                .orElseThrow(() -> new NotFoundException("Published policy was not found"));
    }
}
