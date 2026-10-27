package com.greenocean.backend.assistance.service;

import com.greenocean.backend.assistance.dto.*;
import com.greenocean.backend.assistance.repository.AssistanceRepository;
import com.greenocean.backend.auth.entity.User;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.repository.UserSessionRepository;
import com.greenocean.backend.auth.service.TokenService;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class AssistanceService {
    private static final String GENERIC_RESET_MESSAGE = "If the account exists, recovery instructions have been queued.";
    private final AssistanceRepository repository;
    private final UserRepository users;
    private final UserSessionRepository sessions;
    private final TokenService tokens;
    private final PasswordEncoder passwordEncoder;
    private final DatabaseUuidGenerator uuidGenerator;
    private final OutboxCipher cipher;
    private final com.greenocean.backend.auth.service.AuthRateLimit limits;
    private final SecureRandom random = new SecureRandom();
    private final VerificationPolicy policy;private final com.greenocean.backend.auth.service.SecurityEvents events;
    private final org.springframework.jdbc.core.JdbcTemplate db;
    private final org.springframework.transaction.support.TransactionTemplate transaction;

    public AssistanceService(AssistanceRepository repository, UserRepository users, UserSessionRepository sessions,
                             TokenService tokens, PasswordEncoder passwordEncoder, DatabaseUuidGenerator uuidGenerator, OutboxCipher cipher, com.greenocean.backend.auth.service.AuthRateLimit limits,
                             VerificationPolicy policy,org.springframework.jdbc.core.JdbcTemplate db,org.springframework.transaction.PlatformTransactionManager manager,com.greenocean.backend.auth.service.SecurityEvents events) {
        this.repository = repository; this.users = users; this.sessions = sessions; this.tokens = tokens;
        this.cipher=cipher; this.limits=limits;
        this.passwordEncoder = passwordEncoder; this.uuidGenerator = uuidGenerator;
        this.policy=policy;this.db=db;this.events=events;this.transaction=new org.springframework.transaction.support.TransactionTemplate(manager);
    }

    @Transactional
    public AssistanceResponse requestPasswordReset(EmailRequest request) {
        UUID requestId = uuidGenerator.nextUuid();
        if (!limits.allowInCurrentTransaction("delivery:"+normalize(request.email()),5,java.time.Duration.ofMinutes(15))) return new AssistanceResponse(requestId, GENERIC_RESET_MESSAGE);
        users.lockByEmail(normalize(request.email())).ifPresent(user -> {
            if(user.getStatus()!=com.greenocean.backend.auth.entity.UserStatus.ACTIVE && user.getStatus()!=com.greenocean.backend.auth.entity.UserStatus.PENDING)return;
            events.record(user.getId(),null,com.greenocean.backend.auth.service.SecurityEvents.Event.RESET_REQUESTED);
            if(user.getStatus()==com.greenocean.backend.auth.entity.UserStatus.PENDING)db.update("UPDATE users SET verification_activity_at=CURRENT_TIMESTAMP WHERE id=?",user.getId());
            repository.invalidatePasswordResets(user.getId());
            String token = tokens.createRefreshToken();
            repository.createPasswordReset(requestId, user.getId(), tokens.hashRefreshToken(token), OffsetDateTime.now().plusMinutes(30));
            repository.enqueuePasswordReset(uuidGenerator.nextUuid(), user.getEmail(), cipher.encrypt(token));
        });
        return new AssistanceResponse(requestId, GENERIC_RESET_MESSAGE);
    }

    @Transactional
    public AssistanceResponse resetPassword(ResetPasswordRequest request) {
        com.greenocean.backend.auth.service.PasswordPolicy.validate(request.newPassword());
        String hash=tokens.hashRefreshToken(request.token());
        UUID userId = repository.passwordResetOwner(hash)
                .orElseThrow(() -> new IllegalArgumentException("Recovery token is invalid or expired"));
        User user = users.lockById(userId).orElseThrow(() -> new NotFoundException("User was not found"));
        if(user.getStatus()!=com.greenocean.backend.auth.entity.UserStatus.ACTIVE && user.getStatus()!=com.greenocean.backend.auth.entity.UserStatus.PENDING)throw new IllegalArgumentException("Recovery token is invalid or expired");
        if(repository.consumePasswordReset(hash).isEmpty())throw new IllegalArgumentException("Recovery token is invalid or expired");
        if(user.getStatus()==com.greenocean.backend.auth.entity.UserStatus.PENDING)db.update("UPDATE users SET verification_activity_at=CURRENT_TIMESTAMP WHERE id=?",userId);
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        sessions.revokeAllActiveByUserId(userId, Instant.now());
        events.record(userId,null,com.greenocean.backend.auth.service.SecurityEvents.Event.RESET_COMPLETED);
        return new AssistanceResponse(userId, "Password updated. Existing sessions were revoked.");
    }

    public AssistanceResponse requestEmailVerification(EmailRequest request) {
        UUID requestId = uuidGenerator.nextUuid();
        String email=normalize(request.email());
        var candidate=users.findByEmailIgnoreCase(email);
        String budget="verification-delivery:"+candidate.map(u->u.getId().toString()).orElse(email);
        boolean enclosing=org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive();
        boolean allowed=enclosing?limits.allowInCurrentTransaction(budget,policy.deliveries(),policy.window()):limits.allow(budget,policy.deliveries(),policy.window());
        if(!allowed)return new AssistanceResponse(requestId,queued(requestId).message(),(int)Math.min(3600,policy.window().toSeconds()));
        return transaction.execute(status->{
            var found=users.lockByEmail(email);
            if(found.isEmpty())return queued(requestId);
            var user=found.get();
            if (user.isEmailVerified() || (user.getStatus()!=com.greenocean.backend.auth.entity.UserStatus.ACTIVE && user.getStatus()!=com.greenocean.backend.auth.entity.UserStatus.PENDING)) return queued(requestId);
            if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM email_verification_codes WHERE user_id=? AND used_at IS NULL AND expires_at>CURRENT_TIMESTAMP AND created_at>CURRENT_TIMESTAMP-(? * INTERVAL '1 second'))",Boolean.class,user.getId(),policy.cooldownSeconds())))return queued(requestId);
            var previous=db.queryForList("SELECT code_hash FROM email_verification_codes WHERE user_id=? ORDER BY created_at DESC,id DESC LIMIT 1",String.class,user.getId());
            String code=null;
            for(int attempt=0;attempt<20;attempt++){
                String candidateCode=String.format(Locale.ROOT,"%06d",random.nextInt(1_000_000));
                if(previous.isEmpty()||!cipher.verificationHashes(email,candidateCode).contains(previous.getFirst())){code=candidateCode;break;}
            }
            if(code==null)throw new IllegalStateException("Verification generation is temporarily unavailable");
            repository.invalidateVerificationCodes(user.getId());
            repository.createVerificationCode(requestId, user.getId(), cipher.codeHash(email,code), OffsetDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(15));
            repository.enqueueVerification(uuidGenerator.nextUuid(),user.getId(),requestId,user.getEmail(),cipher.encrypt(code));
            db.update("UPDATE users SET verification_activity_at=CURRENT_TIMESTAMP WHERE id=?",user.getId());
            return queued(requestId);
        });
    }

    private AssistanceResponse queued(UUID id) {
        return new AssistanceResponse(id,"If the account exists and needs verification, a code has been requested. Use the newest email; check Spam. Provider acceptance does not guarantee inbox delivery.",policy.cooldownSeconds());
    }

    public AssistanceResponse verifyEmail(VerifyEmailRequest request) {
        String email=normalize(request.email());
        var candidate=users.findByEmailIgnoreCase(email);
        if (!limits.allow("verify:"+candidate.map(u->u.getId().toString()).orElse(email),policy.attempts(),policy.window()))
            throw new com.greenocean.backend.common.exception.TooManyRequestsException("Verification was not completed. Wait before trying again.",policy.window().toSeconds());
        return transaction.execute(status->{
        User user = users.lockByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Verification code is invalid or expired"));
        if(user.isEmailVerified() || (user.getStatus()!=com.greenocean.backend.auth.entity.UserStatus.PENDING && user.getStatus()!=com.greenocean.backend.auth.entity.UserStatus.ACTIVE))
            throw new IllegalArgumentException("Verification code is invalid or expired");
        if (!repository.consumeVerificationCode(user.getId(), cipher.verificationHashes(email,request.code())))
            throw new IllegalArgumentException("Verification code is invalid or expired");
        repository.verifyUser(user.getId());
        return new AssistanceResponse(user.getId(), "Email address verified.");
        });
    }

    public AssistanceResponse correctPendingEmail(UUID userId,PendingEmailRequest request) {
        if(!limits.allow("email-correction:"+userId,policy.correctionAttempts(),policy.window()))
            throw new com.greenocean.backend.common.exception.TooManyRequestsException("Please wait before trying this account change again",policy.window().toSeconds());
        return transaction.execute(status->{
            var user=users.lockById(userId).orElseThrow(()->new com.greenocean.backend.common.exception.ForbiddenException("Account change could not be completed"));
            if(user.getStatus()!=com.greenocean.backend.auth.entity.UserStatus.PENDING || user.isEmailVerified() || !passwordEncoder.matches(request.password(),user.getPasswordHash()))
                throw new com.greenocean.backend.common.exception.ForbiddenException("Account change could not be completed. Check your password and account status.");
            db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))","registration:"+request.newEmail());
            String message="If the change could be completed, verification was requested for the new address. Sign in again with the intended address or use account recovery.";
            var response=new AssistanceResponse(uuidGenerator.nextUuid(),message,policy.cooldownSeconds());
            if(users.existsByEmailIgnoreCase(request.newEmail()) && !user.getEmail().equalsIgnoreCase(request.newEmail()))return response;
            repository.invalidateVerificationCodes(userId);
            repository.cancelVerificationDeliveries(userId);
            user.correctPendingEmail(request.newEmail());users.saveAndFlush(user);
            db.update("UPDATE users SET verification_activity_at=CURRENT_TIMESTAMP WHERE id=?",userId);
            db.update("INSERT INTO audit_logs(actor_user_id,action,entity_type,entity_id,metadata) VALUES (?,'PENDING_EMAIL_CORRECTED','USER',?,'{}')",userId,userId);
            requestEmailVerification(new EmailRequest(request.newEmail()));
            sessions.revokeAllActiveByUserId(userId,Instant.now());
            return response;
        });
    }

    @Transactional
    public AssistanceResponse support(UUID userId, SupportRequest request) {
        UUID id = uuidGenerator.nextUuid();
        repository.createSupportRequest(id, userId, normalize(request.email()), request.message().trim());
        return new AssistanceResponse(id, "Support request received.");
    }

    private String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }
}
