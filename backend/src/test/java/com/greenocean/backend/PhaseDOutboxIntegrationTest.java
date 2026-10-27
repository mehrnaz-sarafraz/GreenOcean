package com.greenocean.backend;
import com.greenocean.backend.assistance.service.*;
import com.greenocean.backend.auth.service.RegistrationService;
import com.greenocean.backend.auth.dto.RegisterRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test") @ExtendWith(OutputCaptureExtension.class)
class PhaseDOutboxIntegrationTest {
 @Autowired JdbcTemplate db;@Autowired OutboxCipher cipher;@Autowired RegistrationService registration;
 String email;UUID userId,deliveryId;
 @BeforeEach void account(){email="outbox-"+UUID.randomUUID()+"@example.test";registration.register(new RegisterRequest(email,"a synthetic outbox test passphrase",null,null,null,"IR",null,null,true,1));userId=db.queryForObject("SELECT id FROM users WHERE email=?",UUID.class,email);deliveryId=db.queryForObject("SELECT id FROM email_outbox WHERE recipient=?",UUID.class,email);}
 @AfterEach void clean(){db.update("DELETE FROM email_outbox WHERE owner_user_id=?",userId);db.update("DELETE FROM users WHERE id=?",userId);}
 EmailOutboxWorker worker(MailTransport transport){return new EmailOutboxWorker(db,cipher,transport,true,"sender@example.test",5,1,30,null);}
 void due(){db.update("UPDATE email_outbox SET next_attempt_at=CURRENT_TIMESTAMP-INTERVAL '1 second' WHERE id=?",deliveryId);}
 @Test @Tag("happy") void successfulAcceptancePurgesPayloadAndNetworkHasNoOpenTransaction(CapturedOutput output){
   String code=cipher.decrypt(db.queryForObject("SELECT payload->>'encrypted' FROM email_outbox WHERE id=?",String.class,deliveryId));
   worker((id,to,template,secret)->{assertThat(org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()).isFalse();assertThat(id).isEqualTo(deliveryId);assertThat(to).isEqualTo(email);assertThat(template).isEqualTo("email-verification");assertThat(secret).isEqualTo(code);}).deliver();
   assertThat(db.queryForMap("SELECT status,payload::text,delivery_state,attempts FROM email_outbox WHERE id=?",deliveryId)).containsEntry("status","SENT").containsEntry("payload","{}").containsEntry("delivery_state","ACCEPTED").containsEntry("attempts",1);
   assertThat(output.getAll()).doesNotContain(code,email);
 }
 @Test @Tag("confused") void transientFailureBacksOffThenRetriesTheSameDelivery(){
   worker((id,to,template,secret)->{throw new MailDeliveryFailure(MailDeliveryFailure.Kind.RETRYABLE);}).deliver();
   assertThat(db.queryForMap("SELECT status,attempts,next_attempt_at>CURRENT_TIMESTAMP AS future FROM email_outbox WHERE id=?",deliveryId)).containsEntry("status","FAILED").containsEntry("attempts",1).containsEntry("future",true);
   var seen=new ArrayList<UUID>();var retry=worker((id,to,template,secret)->seen.add(id));retry.deliver();assertThat(seen).isEmpty();due();retry.deliver();assertThat(seen).containsExactly(deliveryId);
 }
 @Test @Tag("confused") void permanentFailureAndRetryExhaustionPurgeSensitivePayload(){
   worker((id,to,template,secret)->{throw new MailDeliveryFailure(MailDeliveryFailure.Kind.PERMANENT);}).deliver();
   assertThat(db.queryForMap("SELECT status,payload::text,outcome_code FROM email_outbox WHERE id=?",deliveryId)).containsEntry("status","DEAD").containsEntry("payload","{}").containsEntry("outcome_code","PERMANENT_FAILURE");
 }
 @Test @Tag("creative") void crashedSendingLeaseIsRecoveredAfterRestartWithStableId(){
   db.update("UPDATE email_outbox SET status='SENDING',attempts=1,lease_token=?,lease_until=CURRENT_TIMESTAMP-INTERVAL '1 second' WHERE id=?",UUID.randomUUID(),deliveryId);
   var ids=new ArrayList<UUID>();worker((id,to,template,secret)->ids.add(id)).deliver();assertThat(ids).containsExactly(deliveryId);
   assertThat(db.queryForObject("SELECT attempts FROM email_outbox WHERE id=?",Integer.class,deliveryId)).isEqualTo(2);
 }
 @Test @Tag("malicious") void exhaustedCrashDoesNotDispatchAgainAndPurgesPayload(){
   db.update("UPDATE email_outbox SET status='SENDING',attempts=5,lease_until=CURRENT_TIMESTAMP-INTERVAL '1 second' WHERE id=?",deliveryId);
   worker((id,to,template,secret)->{throw new AssertionError("Exhausted delivery must not dispatch");}).deliver();
   assertThat(db.queryForMap("SELECT status,payload::text FROM email_outbox WHERE id=?",deliveryId)).containsEntry("status","DEAD").containsEntry("payload","{}");
 }
 @Test @Tag("confused") void uncertainAcceptanceIsReconciledUsingTheSameIdempotencyKey(){
   var accepted=new HashSet<UUID>();var attempts=new java.util.concurrent.atomic.AtomicInteger();
   var w=worker((id,to,template,secret)->{accepted.add(id);if(attempts.incrementAndGet()==1)throw new MailDeliveryFailure(MailDeliveryFailure.Kind.UNCERTAIN);});
   w.deliver();assertThat(db.queryForObject("SELECT outcome_code FROM email_outbox WHERE id=?",String.class,deliveryId)).isEqualTo("ACCEPTANCE_UNCERTAIN");due();w.deliver();
   assertThat(accepted).containsExactly(deliveryId);assertThat(attempts.get()).isEqualTo(2);assertThat(db.queryForObject("SELECT status FROM email_outbox WHERE id=?",String.class,deliveryId)).isEqualTo("SENT");
 }
 @Test @Tag("malicious") void twoInstancesCannotClaimTheSameLiveLease()throws Exception{
   var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var count=new java.util.concurrent.atomic.AtomicInteger();
   MailTransport mail=(id,to,template,secret)->{count.incrementAndGet();entered.countDown();try{release.await(10,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}};
   try(var pool=Executors.newFixedThreadPool(2)){
     var first=pool.submit(()->worker(mail).deliver());assertThat(entered.await(5,TimeUnit.SECONDS)).isTrue();
     var second=pool.submit(()->worker(mail).deliver());second.get(5,TimeUnit.SECONDS);release.countDown();first.get(5,TimeUnit.SECONDS);
   }finally{release.countDown();}
   assertThat(count.get()).isEqualTo(1);
 }
 @Test @Tag("malicious") void expiredAndSupersededMessagesDoNotSend(){
   db.update("UPDATE email_outbox SET expires_at=CURRENT_TIMESTAMP WHERE id=?",deliveryId);
   worker((id,to,template,secret)->{throw new AssertionError("Expired message sent");}).deliver();
   assertThat(db.queryForMap("SELECT status,payload::text FROM email_outbox WHERE id=?",deliveryId)).containsEntry("status","DEAD").containsEntry("payload","{}");
 }
 @Test @Tag("malicious") void retiredChallengeCannotBeDelivered(){
   db.update("UPDATE email_verification_codes SET used_at=CURRENT_TIMESTAMP WHERE user_id=?",userId);
   worker((id,to,template,secret)->{throw new AssertionError("Retired message sent");}).deliver();
   assertThat(db.queryForObject("SELECT status FROM email_outbox WHERE id=?",String.class,deliveryId)).isEqualTo("CANCELLED");
 }
 @Test @Tag("malicious") void workerWithOldLeaseCannotOverwriteNewWorkerState(){
   UUID replacement=UUID.randomUUID();worker((id,to,template,secret)->db.update("UPDATE email_outbox SET lease_token=? WHERE id=?",replacement,id)).deliver();
   assertThat(db.queryForMap("SELECT status,lease_token FROM email_outbox WHERE id=?",deliveryId)).containsEntry("status","SENDING").containsEntry("lease_token",replacement);
 }
 @Test @Tag("confused") void missingRotationKeyFailsReadinessExplicitlyBeforeDispatch(){
   var wrong=new OutboxCipher("another-synthetic-key","other","");
   assertThatThrownBy(()->new DeliveryKeyReadiness(db,wrong).run(null)).isInstanceOf(IllegalStateException.class).hasMessageContaining("key readiness failed");
   assertThat(db.queryForObject("SELECT status FROM email_outbox WHERE id=?",String.class,deliveryId)).isEqualTo("PENDING");
 }
}
