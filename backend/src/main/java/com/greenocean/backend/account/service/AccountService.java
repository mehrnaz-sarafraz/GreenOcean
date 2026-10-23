package com.greenocean.backend.account.service;

import com.greenocean.backend.account.dto.AccountArchiveResponse;
import com.greenocean.backend.account.repository.AccountRepository;
import com.greenocean.backend.moderation.service.ReportService;
import com.greenocean.backend.preference.service.UserPreferencesService;
import com.greenocean.backend.profile.service.ProfileService;
import com.greenocean.backend.social.service.SocialService;
import com.greenocean.backend.wellbeing.service.WellbeingService;
import com.greenocean.backend.search.service.SearchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.exception.ForbiddenException;
import com.greenocean.backend.professional.service.ProfessionalDocumentStorage;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AccountService {
    private final ProfileService profiles;
    private final UserPreferencesService preferences;
    private final WellbeingService wellbeing;
    private final ReportService reports;
    private final SocialService social;
    private final AccountRepository repository;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final ProfessionalDocumentStorage documentStorage;
    private final SearchService search;

    public AccountService(ProfileService profiles, UserPreferencesService preferences, WellbeingService wellbeing,
                          ReportService reports, SocialService social, AccountRepository repository,
                          UserRepository users, PasswordEncoder passwordEncoder,
                          ProfessionalDocumentStorage documentStorage, SearchService search) {
        this.profiles = profiles;
        this.preferences = preferences;
        this.wellbeing = wellbeing;
        this.reports = reports;
        this.social = social;
        this.repository = repository;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.documentStorage = documentStorage;
        this.search = search;
    }

    @Transactional(readOnly = true)
    public AccountArchiveResponse archive(UUID userId) {
        return new AccountArchiveResponse(Instant.now(), profiles.getOwnProfile(userId), preferences.get(userId),
                wellbeing.snapshot(userId), reports.mine(userId), social.following(userId), social.followers(userId),
                social.blocked(userId), search.history(userId), repository.activity(userId));
    }

    @Transactional
    public void delete(UUID userId, String password) {
        var user = users.findById(userId).orElseThrow(() -> new NotFoundException("Account was not found"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) throw new ForbiddenException("Password is incorrect");
        var keys = repository.professionalDocumentKeys(userId);
        repository.erase(userId);
        keys.forEach(documentStorage::delete);
    }
}
