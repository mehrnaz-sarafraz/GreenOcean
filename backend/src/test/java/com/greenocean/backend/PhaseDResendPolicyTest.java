package com.greenocean.backend;
import com.greenocean.backend.assistance.service.*;
import com.greenocean.backend.assistance.dto.*;
import com.greenocean.backend.auth.service.RegistrationService;
import com.greenocean.backend.auth.dto.RegisterRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test") @Transactional
@TestPropertySource(properties="greenocean.verification.resend-cooldown-seconds=60")
class PhaseDResendPolicyTest {
 @Autowired JdbcTemplate db;@Autowired AssistanceService assistance;@Autowired RegistrationService registration;
 @Test @Tag("confused") void cooldownLimitsSendingWithoutRevealingAccountExistenceAndAllowsRecovery(){
   String email="cooldown-"+UUID.randomUUID()+"@example.test";
   registration.register(new RegisterRequest(email,"A synthetic cooldown test passphrase",null,null,null,"IR",null,null,true,1));
   UUID id=db.queryForObject("SELECT id FROM users WHERE email=?",UUID.class,email);
   var existing=assistance.requestEmailVerification(new EmailRequest(email));var absent=assistance.requestEmailVerification(new EmailRequest("absent@example.test"));
   assertThat(existing.message()).isEqualTo(absent.message());assertThat(existing.retryAfterSeconds()).isEqualTo(60);assertThat(absent.retryAfterSeconds()).isEqualTo(60);
   assertThat(db.queryForObject("SELECT count(*) FROM email_verification_codes WHERE user_id=?",Integer.class,id)).isEqualTo(1);
   db.update("UPDATE email_verification_codes SET created_at=CURRENT_TIMESTAMP-INTERVAL '61 seconds' WHERE user_id=?",id);
   assistance.requestEmailVerification(new EmailRequest(email));
   assertThat(db.queryForObject("SELECT count(*) FROM email_verification_codes WHERE user_id=? AND used_at IS NULL",Integer.class,id)).isEqualTo(1);
   assertThat(db.queryForObject("SELECT count(*) FROM email_verification_codes WHERE user_id=? AND used_at IS NOT NULL",Integer.class,id)).isEqualTo(1);
 }
}
