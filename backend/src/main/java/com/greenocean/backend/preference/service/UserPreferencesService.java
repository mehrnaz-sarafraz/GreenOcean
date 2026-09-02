package com.greenocean.backend.preference.service;

import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.preference.dto.UpdateUserPreferencesRequest;
import com.greenocean.backend.preference.dto.UserPreferencesResponse;
import com.greenocean.backend.preference.repository.UserPreferencesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserPreferencesService {
    private final UserPreferencesRepository repository;
    public UserPreferencesService(UserPreferencesRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public UserPreferencesResponse get(UUID userId) {
        return repository.find(userId).orElseThrow(() -> new NotFoundException("User preferences were not found"));
    }

    @Transactional
    public UserPreferencesResponse update(UUID userId, UpdateUserPreferencesRequest request) {
        repository.update(userId, request);
        return get(userId);
    }

    @Transactional
    public void recordMood(UUID userId, String mood) { repository.recordMood(userId, mood); }
}
