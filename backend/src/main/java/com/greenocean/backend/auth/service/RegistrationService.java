package com.greenocean.backend.auth.service;

import com.greenocean.backend.auth.dto.*;
import com.greenocean.backend.auth.entity.*;
import com.greenocean.backend.auth.repository.*;
import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.profile.entity.Profile;
import com.greenocean.backend.profile.repository.ProfileRepository;
import com.greenocean.backend.identity.AccessPolicy;
import com.greenocean.backend.preference.repository.PolicyRepository;
import com.greenocean.backend.assistance.service.AssistanceService;
import com.greenocean.backend.assistance.dto.EmailRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Year;
import java.util.*;

@Service
public class RegistrationService {
    private final UserRepository users;
    private final ProfileRepository profiles;
    private final RoleRepository roles;
    private final PasswordEncoder passwords;
    private final DatabaseUuidGenerator ids;
    private final AccessPolicy access;
    private final PolicyRepository policies;
    private final JdbcTemplate db;
    private final AssistanceService assistance;
    public RegistrationService(UserRepository users, ProfileRepository profiles, RoleRepository roles,
       PasswordEncoder passwords, DatabaseUuidGenerator ids, AccessPolicy access, PolicyRepository policies,
       JdbcTemplate db, AssistanceService assistance) {
        this.users=users; this.profiles=profiles; this.roles=roles; this.passwords=passwords; this.ids=ids;
        this.access=access; this.policies=policies; this.db=db; this.assistance=assistance;
    }
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        PasswordPolicy.validate(request.password());
        if (!Boolean.TRUE.equals(request.adultSelfAttested()) ||
            (request.birthYear()!=null && request.birthYear()>Year.now(java.time.ZoneOffset.UTC).getValue()-18))
            throw new IllegalArgumentException("Social accounts are for adults 18+. Public resources are available without an account.");
        var terms=policies.latestPublished("terms").orElseThrow(() -> new IllegalStateException("Published terms are unavailable"));
        if (!Objects.equals(request.termsVersion(),terms.version())) throw new IllegalArgumentException("Review and accept the current Terms version");
        String country=request.countryCode().trim().toUpperCase(Locale.ROOT);
        access.country(country);
        String email=request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) throw new ConflictException("Unable to create account. Try account recovery or sign in.");
        String username="member_"+UUID.randomUUID().toString().replace("-", "").substring(0,22);
        var user=new User(ids.nextUuid(),email,passwords.encode(request.password()));
        user.enrollPhaseA();
        user.assignRole(roles.findByName("USER").orElseThrow());
        users.saveAndFlush(user);
        profiles.saveAndFlush(new Profile(user,username,"New member",null,null,null));
        db.update("INSERT INTO account_eligibility(user_id,age_result,age_method,checked_at,residence_country) VALUES (?,'ADULT','SELF_ATTESTED',CURRENT_TIMESTAMP,?)",user.getId(),country);
        db.update("INSERT INTO policy_acceptances(user_id,policy_key,version) VALUES (?,'terms',?)",user.getId(),terms.version());
        assistance.requestEmailVerification(new EmailRequest(email));
        return new RegisterResponse(user.getId(),email,username);
    }
}
