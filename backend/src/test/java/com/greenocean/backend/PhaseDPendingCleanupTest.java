package com.greenocean.backend;
import com.greenocean.backend.account.service.PendingAccountCleanup;
import com.greenocean.backend.account.repository.AccountRepository;
import com.greenocean.backend.assistance.service.AssistanceService;
import com.greenocean.backend.assistance.dto.EmailRequest;
import com.greenocean.backend.auth.service.RegistrationService;
import com.greenocean.backend.auth.dto.RegisterRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test")
class PhaseDPendingCleanupTest {
 @Autowired JdbcTemplate db;@Autowired AccountRepository accounts;@Autowired PlatformTransactionManager manager;@Autowired RegistrationService registration;@Autowired AssistanceService assistance;@Autowired com.greenocean.backend.assistance.service.OutboxCipher cipher;
 List<UUID> ids=new ArrayList<>();Map<UUID,String> issuedCodes=new HashMap<>();
 UUID account(boolean old){String email="cleanup-"+UUID.randomUUID()+"@example.test";registration.register(new RegisterRequest(email,"a synthetic cleanup account passphrase",null,null,null,"IR",null,null,true,1));UUID id=db.queryForObject("SELECT id FROM users WHERE email=?",UUID.class,email);ids.add(id);issuedCodes.put(id,cipher.decrypt(db.queryForObject("SELECT payload->>'encrypted' FROM email_outbox WHERE owner_user_id=?",String.class,id)));if(old){db.update("UPDATE users SET created_at=CURRENT_TIMESTAMP-INTERVAL '8 days',verification_activity_at=CURRENT_TIMESTAMP-INTERVAL '8 days',last_login_at=NULL WHERE id=?",id);db.update("UPDATE email_outbox SET status='SENT',delivery_state='ACCEPTED',payload='{}',created_at=CURRENT_TIMESTAMP-INTERVAL '8 days',sent_at=CURRENT_TIMESTAMP-INTERVAL '8 days' WHERE owner_user_id=?",id);}return id;}
 PendingAccountCleanup cleanup(boolean dry,boolean pause,int batch){return new PendingAccountCleanup(db,accounts,manager,true,dry,pause,java.time.Duration.ofDays(7),batch);}
 @AfterEach void clean(){for(UUID id:ids){db.update("DELETE FROM audit_logs WHERE actor_user_id=?",id);db.update("DELETE FROM email_outbox WHERE owner_user_id=?",id);db.update("DELETE FROM account_deletions WHERE user_id=?",id);db.update("DELETE FROM users WHERE id=?",id);}}
 @Test @Tag("happy") void oldAbandonedAccountIsAnonymizedUsingExistingLifecycleAndCleanupIsIdempotent(){
   UUID id=account(true);assertThat(cleanup(false,false,10).run().erased()).isEqualTo(1);
   assertThat(db.queryForMap("SELECT status,email,password_hash FROM users WHERE id=?",id)).containsEntry("status","DELETED").containsEntry("password_hash","DELETED");
   assertThat(db.queryForObject("SELECT email FROM users WHERE id=?",String.class,id)).endsWith("@greenocean.invalid");
   for(String table:List.of("email_outbox","email_verification_codes","password_reset_tokens","user_sessions","account_eligibility","policy_acceptances","user_preferences")){
     String column=table.equals("email_outbox")?"owner_user_id":"user_id";
     assertThat(db.queryForObject("SELECT count(*) FROM "+table+" WHERE "+column+"=?",Integer.class,id)).isZero();
   }
   assertThat(db.queryForObject("SELECT display_name FROM profiles WHERE user_id=?",String.class,id)).isEqualTo("Deleted member");assertThat(cleanup(false,false,10).run().erased()).isZero();
 }
 @Test @Tag("malicious") void activeLegacySuspendedNewAndHistoricalAccountsAreNotSelected(){
   UUID active=account(true),legacy=account(true),suspended=account(true),recent=account(false),historical=account(true);
   db.update("UPDATE users SET status='ACTIVE' WHERE id=?",active);db.update("UPDATE users SET phase_a_enrolled=FALSE,status='ACTIVE' WHERE id=?",legacy);db.update("UPDATE users SET status='SUSPENDED' WHERE id=?",suspended);
   db.update("INSERT INTO listener_applications(user_id,motivation,experience,weekly_availability,accepted_code,status) VALUES (?,'Synthetic historical application','Synthetic experience','1 hour',TRUE,'SUBMITTED')",historical);
   assertThat(cleanup(false,false,10).run().erased()).isZero();assertThat(db.queryForObject("SELECT status FROM users WHERE id=?",String.class,recent)).isEqualTo("PENDING");
 }
 @Test @Tag("confused") void recentResendAndUndeliveredMailProtectAccountsFromRetention(){
   UUID resent=account(true),outage=account(true);String email=db.queryForObject("SELECT email FROM users WHERE id=?",String.class,resent);
   assistance.requestEmailVerification(new EmailRequest(email));db.update("UPDATE email_outbox SET status='DEAD',delivery_state=NULL,outcome_code='RETRY_EXHAUSTED' WHERE owner_user_id=?",outage);
   assertThat(cleanup(false,false,10).run().erased()).isZero();
 }
 @Test @Tag("creative") void dryRunPauseAndBoundedRestartAreSafe(){
   UUID first=account(true);account(true);assertThat(cleanup(true,false,1).run().candidates()).isEqualTo(1);
   assertThat(cleanup(false,true,1).run().paused()).isTrue();assertThat(db.queryForObject("SELECT status FROM users WHERE id=?",String.class,first)).isEqualTo("PENDING");
   assertThat(cleanup(false,false,1).run().erased()).isEqualTo(1);assertThat(cleanup(false,false,1).run().erased()).isEqualTo(1);
 }
 @Test @Tag("creative") void authorizedOperationHoldingAccountLockIsSkipped()throws Exception{
   UUID id=account(true);var locked=new CountDownLatch(1);var release=new CountDownLatch(1);
   try(var pool=Executors.newSingleThreadExecutor()){
     var operation=pool.submit(()->new org.springframework.transaction.support.TransactionTemplate(manager).execute(s->{db.queryForList("SELECT id FROM users WHERE id=? FOR UPDATE",id);locked.countDown();try{release.await(10,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}assistance.verifyEmail(new com.greenocean.backend.assistance.dto.VerifyEmailRequest(db.queryForObject("SELECT email FROM users WHERE id=?",String.class,id),issuedCodes.get(id)));return true;}));
     assertThat(locked.await(5,TimeUnit.SECONDS)).isTrue();assertThat(cleanup(false,false,10).run().erased()).isZero();release.countDown();operation.get(5,TimeUnit.SECONDS);
   }finally{release.countDown();}
   assertThat(db.queryForObject("SELECT status FROM users WHERE id=?",String.class,id)).isEqualTo("ACTIVE");assertThat(cleanup(false,false,10).run().erased()).isZero();
 }
 @Test @Tag("confused") void bouncedDeliveryAndLiveRecoveryCredentialProtectOldAccounts(){
   UUID bounced=account(true),recovering=account(true);
   db.update("UPDATE email_outbox SET delivery_state='BOUNCED' WHERE owner_user_id=?",bounced);
   db.update("INSERT INTO password_reset_tokens(id,user_id,token_hash,expires_at) VALUES (?,?,?,CURRENT_TIMESTAMP+INTERVAL '10 minutes')",UUID.randomUUID(),recovering,"synthetic-recovery-"+UUID.randomUUID());
   assertThat(cleanup(false,false,10).run().erased()).isZero();
 }
 @Test @Tag("creative") void sevenDayBoundaryUsesServerTimeAndConfigurableThreshold(){
   UUID id=account(true);var tx=new org.springframework.transaction.support.TransactionTemplate(manager);
   tx.executeWithoutResult(status->{
     db.update("UPDATE users SET created_at=CURRENT_TIMESTAMP-INTERVAL '7 days',verification_activity_at=CURRENT_TIMESTAMP-INTERVAL '7 days' WHERE id=?",id);
     assertThat(new PendingAccountCleanup(db,accounts,manager,true,true,false,java.time.Duration.ofDays(8),10).run().candidates()).isZero();
     assertThat(cleanup(true,false,10).run().candidates()).isEqualTo(1);
     db.update("UPDATE users SET verification_activity_at=CURRENT_TIMESTAMP-INTERVAL '7 days'+INTERVAL '1 second' WHERE id=?",id);
     assertThat(cleanup(true,false,10).run().candidates()).isZero();
   });
 }
 @Test @Tag("confused") void erasureTransactionFailureRollsBackAndCanBeRetried(){
   UUID id=account(true);String suffix=UUID.randomUUID().toString().replace("-","");String function="qa_cleanup_"+suffix,trigger="qa_cleanup_trigger_"+suffix;
   db.execute("CREATE FUNCTION "+function+"() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.id='"+id+"'::uuid AND NEW.status='DELETED' THEN RAISE EXCEPTION 'synthetic cleanup failure'; END IF; RETURN NEW; END $$");
   db.execute("CREATE TRIGGER "+trigger+" BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION "+function+"()");
   try{assertThat(cleanup(false,false,1).run().failures()).isEqualTo(1);assertThat(db.queryForObject("SELECT status FROM users WHERE id=?",String.class,id)).isEqualTo("PENDING");assertThat(db.queryForObject("SELECT count(*) FROM email_outbox WHERE owner_user_id=?",Integer.class,id)).isEqualTo(1);}
   finally{db.execute("DROP TRIGGER "+trigger+" ON users");db.execute("DROP FUNCTION "+function+"()");}
   assertThat(cleanup(false,false,1).run().erased()).isEqualTo(1);
 }
}
