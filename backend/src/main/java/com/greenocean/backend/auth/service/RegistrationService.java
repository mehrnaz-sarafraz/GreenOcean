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
        try { PasswordPolicy.validate(request.password()); }
        catch (IllegalArgumentException e) { throw new com.greenocean.backend.common.exception.RegistrationValidationException("password", e.getMessage()); }
        if (!Boolean.TRUE.equals(request.adultSelfAttested()) ||
            (request.birthYear()!=null && request.birthYear()>Year.now(java.time.ZoneOffset.UTC).getValue()-18))
            throw new com.greenocean.backend.common.exception.RegistrationValidationException("adultSelfAttested", "Social accounts are for adults 18+. Public resources are available without an account.");
        var terms=policies.latestPublished("terms").orElseThrow(() -> new IllegalStateException("Published terms are unavailable"));
        if (!Objects.equals(request.termsVersion(),terms.version())) throw new com.greenocean.backend.common.exception.RegistrationValidationException("termsVersion", "The Terms have changed. Review and accept the current version before trying again.");
        String country=request.countryCode().trim().toUpperCase(Locale.ROOT);
        try { access.country(country); }
        catch (IllegalArgumentException e) { throw new com.greenocean.backend.common.exception.RegistrationValidationException("countryCode", e.getMessage()); }
        String email=request.email().trim().toLowerCase(Locale.ROOT);
        // Hash on both paths: avoid the obvious existing-account timing shortcut.
        String hash=passwords.encode(request.password());
        // Serialize same-email attempts across application instances. The unique index remains authoritative.
        db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", "registration:"+email);
        if (users.existsByEmailIgnoreCase(email)) return RegisterResponse.pending();
        String username;
        do {
            username="member_"+UUID.randomUUID().toString().replace("-", "").substring(0,22);
            db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", "registration-username:"+username);
        } while (Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM profiles WHERE lower(username)=lower(?))",Boolean.class,username)));
        var user=new User(ids.nextUuid(),email,hash);
        user.enrollPhaseA();
        user.assignRole(roles.findByName("USER").orElseThrow());
        users.saveAndFlush(user);
        profiles.saveAndFlush(new Profile(user,username,"New member",null,null,null));
        db.update("INSERT INTO account_eligibility(user_id,age_result,age_method,checked_at,residence_country) VALUES (?,'ADULT','SELF_ATTESTED',CURRENT_TIMESTAMP,?)",user.getId(),country);
        db.update("INSERT INTO policy_acceptances(user_id,policy_key,version) VALUES (?,'terms',?)",user.getId(),terms.version());
        assistance.requestEmailVerification(new EmailRequest(email));
        return RegisterResponse.pending();
    }
}
