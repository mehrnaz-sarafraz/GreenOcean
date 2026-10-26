package com.greenocean.backend;
import com.greenocean.backend.assistance.service.*;
import com.greenocean.backend.assistance.dto.*;
import com.greenocean.backend.auth.service.*;
import com.greenocean.backend.auth.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test")
class PhaseDConcurrencyTest {
 @Autowired JdbcTemplate db;@Autowired RegistrationService registration;@Autowired AssistanceService assistance;@Autowired OutboxCipher cipher;@Autowired AuthenticationService auth;
 String email,password="A synthetic concurrent verification phrase";UUID id;
 @BeforeEach void account(){email="race-"+UUID.randomUUID()+"@example.test";registration.register(new RegisterRequest(email,password,null,null,null,"IR",null,null,true,1));id=db.queryForObject("SELECT id FROM users WHERE email=?",UUID.class,email);}
 @AfterEach void clean(){db.update("DELETE FROM audit_logs WHERE actor_user_id=?",id);db.update("DELETE FROM email_outbox WHERE owner_user_id=?",id);db.update("DELETE FROM users WHERE id=?",id);}
 String code(){return cipher.decrypt(db.queryForObject("SELECT payload->>'encrypted' FROM email_outbox WHERE owner_user_id=? AND jsonb_exists(payload,'encrypted') ORDER BY created_at DESC,id DESC LIMIT 1",String.class,id));}
 @Test @Tag("malicious") void tenConcurrentConfirmationsHaveExactlyOneWinnerAndOneConsumption()throws Exception{
   String value=code();var start=new CountDownLatch(1);var answers=new ArrayList<Future<Boolean>>();
   try(var pool=Executors.newFixedThreadPool(10)){
     for(int n=0;n<10;n++)answers.add(pool.submit(()->{start.await(5,TimeUnit.SECONDS);try{assistance.verifyEmail(new VerifyEmailRequest(email,value));return true;}catch(IllegalArgumentException|com.greenocean.backend.common.exception.TooManyRequestsException expected){return false;}}));
     start.countDown();int success=0;for(var answer:answers)if(answer.get(30,TimeUnit.SECONDS))success++;assertThat(success).isEqualTo(1);
   }
   assertThat(db.queryForObject("SELECT count(*) FROM email_verification_codes WHERE user_id=? AND used_at IS NOT NULL",Integer.class,id)).isEqualTo(1);
   assertThat(db.queryForMap("SELECT status,email_verified FROM users WHERE id=?",id)).containsEntry("status","ACTIVE").containsEntry("email_verified",true);
 }
 @Test @Tag("creative") void concurrentResendsKeepOneCurrentChallengeAndOneUsefulOutboxEntry()throws Exception{
   var start=new CountDownLatch(1);
   try(var pool=Executors.newFixedThreadPool(2)){
     var a=pool.submit(()->{start.await();return assistance.requestEmailVerification(new EmailRequest(email));});
     var b=pool.submit(()->{start.await();return assistance.requestEmailVerification(new EmailRequest(email));});start.countDown();a.get(15,TimeUnit.SECONDS);b.get(15,TimeUnit.SECONDS);
   }
   assertThat(db.queryForObject("SELECT count(*) FROM email_verification_codes WHERE user_id=? AND used_at IS NULL",Integer.class,id)).isEqualTo(1);
   assertThat(db.queryForObject("SELECT count(*) FROM email_outbox WHERE owner_user_id=? AND status IN ('PENDING','SENDING','FAILED')",Integer.class,id)).isEqualTo(1);
 }
 @Test @Tag("malicious") void concurrentEmailChangesRemainPendingAndNeverShareAccountIdentity()throws Exception{
   String one="one-"+UUID.randomUUID()+"@example.test",two="two-"+UUID.randomUUID()+"@example.test";var start=new CountDownLatch(1);
   try(var pool=Executors.newFixedThreadPool(2)){
     var a=pool.submit(()->{start.await();return assistance.correctPendingEmail(id,new PendingEmailRequest(one,password));});
     var b=pool.submit(()->{start.await();return assistance.correctPendingEmail(id,new PendingEmailRequest(two,password));});start.countDown();a.get(15,TimeUnit.SECONDS);b.get(15,TimeUnit.SECONDS);
   }
   assertThat(db.queryForObject("SELECT email FROM users WHERE id=?",String.class,id)).isIn(one,two);
   assertThat(db.queryForMap("SELECT status,email_verified FROM users WHERE id=?",id)).containsEntry("status","PENDING").containsEntry("email_verified",false);
   assertThat(db.queryForObject("SELECT count(*) FROM email_verification_codes WHERE user_id=? AND used_at IS NULL",Integer.class,id)).isEqualTo(1);
 }
 @Test @Tag("confused") void activationDatabaseFailureRollsBackConsumedCodeAndAllowsRetry(){
   String value=code(),suffix=UUID.randomUUID().toString().replace("-",""),function="qa_verify_"+suffix,trigger="qa_verify_trigger_"+suffix;
   db.execute("CREATE FUNCTION "+function+"() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.id='"+id+"'::uuid AND NEW.email_verified THEN RAISE EXCEPTION 'synthetic activation failure'; END IF; RETURN NEW; END $$");
   db.execute("CREATE TRIGGER "+trigger+" BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION "+function+"()");
   try{assertThatThrownBy(()->assistance.verifyEmail(new VerifyEmailRequest(email,value))).isInstanceOf(RuntimeException.class);assertThat(db.queryForObject("SELECT used_at IS NULL FROM email_verification_codes WHERE user_id=?",Boolean.class,id)).isTrue();assertThat(db.queryForObject("SELECT email_verified FROM users WHERE id=?",Boolean.class,id)).isFalse();}
   finally{db.execute("DROP TRIGGER "+trigger+" ON users");db.execute("DROP FUNCTION "+function+"()");}
   assistance.verifyEmail(new VerifyEmailRequest(email,value));assertThat(db.queryForObject("SELECT email_verified FROM users WHERE id=?",Boolean.class,id)).isTrue();
 }
}
