package com.greenocean.backend.auth.service;

import com.greenocean.backend.auth.dto.RegisterRequest;
import com.greenocean.backend.auth.dto.RegisterResponse;
import com.greenocean.backend.auth.entity.Role;
import com.greenocean.backend.auth.entity.User;
import com.greenocean.backend.auth.repository.RoleRepository;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.profile.entity.Profile;
import com.greenocean.backend.profile.repository.ProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.Locale;

@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DatabaseUuidGenerator databaseUuidGenerator;
    private final EmailVerificationService emailVerificationService;

    public RegistrationService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            DatabaseUuidGenerator databaseUuidGenerator,
            EmailVerificationService emailVerificationService
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.databaseUuidGenerator = databaseUuidGenerator;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        String countryCode = request.countryCode().trim().toUpperCase(Locale.ROOT);
        String city = request.city().trim();
        String displayName = request.displayName().trim();

        if (request.birthYear() > Year.now().getValue()) {
            throw new IllegalArgumentException("birthYear cannot be in the future");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered");
        }
        if (profileRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username is already taken");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("USER role is missing from the database"));

        User user = new User(databaseUuidGenerator.nextUuid(), email, passwordEncoder.encode(request.password()));
        user.assignRole(userRole);
        User savedUser = userRepository.saveAndFlush(user);

        Profile profile = new Profile(savedUser, username, displayName, countryCode, city, request.birthYear());
        profileRepository.save(profile);

        emailVerificationService.createToken(savedUser);

        return new RegisterResponse(savedUser.getId(), savedUser.getEmail(), profile.getUsername());
    }
}
