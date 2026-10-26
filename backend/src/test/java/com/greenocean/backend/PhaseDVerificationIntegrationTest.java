package com.greenocean.backend;
import com.greenocean.backend.assistance.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseDVerificationIntegrationTest {
 @Autowired MockMvc mvc;@Autowired JdbcTemplate db;@Autowired ObjectMapper json;@Autowired OutboxCipher cipher;@Autowired AssistanceService assistance;
 @Autowired jakarta.persistence.EntityManager entities;@Autowired org.springframework.transaction.PlatformTransactionManager manager;
 String password="A synthetic Phase D verification passphrase";
 @BeforeEach void rates(){var tx=new org.springframework.transaction.support.TransactionTemplate(manager);tx.setPropagationBehavior(3);tx.executeWithoutResult(s->db.update("DELETE FROM auth_rate_windows"));}
 Map<String,Object> input(){return new HashMap<>(Map.of("email","d-"+UUID.randomUUID()+"@example.test","password",password,"countryCode","IR","adultSelfAttested",true,"termsVersion",1));}
 ResultActions postJson(String route,Object body)throws Exception{return mvc.perform(post(route).contentType("application/json").content(json.writeValueAsString(body)));}
 UUID register(Map<String,Object> in)throws Exception{postJson("/api/v1/auth/register",in).andExpect(status().isCreated());return db.queryForObject("SELECT id FROM users WHERE email=?",UUID.class,in.get("email"));}
 String code(Map<String,Object> in){return cipher.decrypt(db.queryForObject("SELECT payload->>'encrypted' FROM email_outbox WHERE recipient=? AND template_key='email-verification' AND status<>'CANCELLED' ORDER BY created_at DESC,id DESC LIMIT 1",String.class,in.get("email")));}
 String login(Map<String,Object> in)throws Exception{return json.readTree(postJson("/api/v1/auth/login",Map.of("email",in.get("email"),"password",password)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("accessToken").asText();}
 @Test @Tag("happy") void verificationKeepsTheExistingEligibilityAndServiceBoundaries()throws Exception{
   var in=input();UUID id=register(in);String value=code(in);
   postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",value)).andExpect(status().isOk());entities.clear();
   assertThat(db.queryForMap("SELECT status,email_verified FROM users WHERE id=?",id)).containsEntry("status","ACTIVE").containsEntry("email_verified",true);
   String token=login(in);mvc.perform(get("/api/v1/posts/feed").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
   postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",value)).andExpect(status().isBadRequest());
 }
 @Test @Tag("creative") void leadingZeroCodeIsStoredProtectedAndAcceptedAsSixCharacters()throws Exception{
   Object target=org.springframework.test.util.AopTestUtils.getTargetObject(assistance);
   Object original=org.springframework.test.util.ReflectionTestUtils.getField(target,"random");
   try{
     org.springframework.test.util.ReflectionTestUtils.setField(target,"random",new java.security.SecureRandom(){public int nextInt(int bound){return 7;}});
     var in=input();UUID id=register(in);assertThat(code(in)).isEqualTo("000007");
     assertThat(db.queryForObject("SELECT code_hash FROM email_verification_codes WHERE user_id=? AND used_at IS NULL",String.class,id)).startsWith("v2:").doesNotContain("000007");
     postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code","000007")).andExpect(status().isOk());
   }finally{org.springframework.test.util.ReflectionTestUtils.setField(target,"random",original);}
 }
 @Test @Tag("confused") void blankIncompleteAndMalformedCodesDoNotConsumeAChallenge()throws Exception{
   var in=input();UUID id=register(in);
   for(String value:List.of("","123","abcdef","۱۲۳۴۵۶"))postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",value)).andExpect(status().isBadRequest());
   assertThat(db.queryForObject("SELECT count(*) FROM email_verification_codes WHERE user_id=? AND used_at IS NULL",Integer.class,id)).isEqualTo(1);
 }
 @Test @Tag("creative") void expirationUsesServerTimeAtBeforeAndAfterBoundary()throws Exception{
   for(String delta:List.of("INTERVAL '1 second'","INTERVAL '0 seconds'","-INTERVAL '1 second'")){
     var in=input();UUID id=register(in);String value=code(in);
     db.update("UPDATE email_verification_codes SET expires_at=CURRENT_TIMESTAMP+"+delta+" WHERE user_id=?",id);
     var result=postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",value,"clientTime","2099-01-01T00:00:00Z"));
     if(delta.equals("INTERVAL '1 second'"))result.andExpect(status().isOk());else result.andExpect(status().isBadRequest());
   }
 }
 @Test @Tag("malicious") void suspendedAndDeletedAccountsNeverConsumeOrReportVerificationSuccess()throws Exception{
   for(String state:List.of("SUSPENDED","DELETED")){
     var in=input();UUID id=register(in);String value=code(in);db.update("UPDATE users SET status=? WHERE id=?",state,id);entities.clear();
     postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",value)).andExpect(status().isBadRequest());
     assertThat(db.queryForMap("SELECT status,email_verified FROM users WHERE id=?",id)).containsEntry("status",state).containsEntry("email_verified",false);
     assertThat(db.queryForObject("SELECT used_at IS NULL FROM email_verification_codes WHERE user_id=?",Boolean.class,id)).isTrue();
   }
 }
 @Test @Tag("confused") void resendInvalidatesPriorCodeAndRetainsOneUsefulChallenge()throws Exception{
   var in=input();UUID id=register(in);String old=code(in);
   postJson("/api/v1/account-assistance/email-verification",Map.of("email",in.get("email"))).andExpect(status().isAccepted());
   assertThat(db.queryForObject("SELECT count(*) FROM email_verification_codes WHERE user_id=? AND used_at IS NULL",Integer.class,id)).isEqualTo(1);
   String current=code(in);
   assertThat(current).isNotEqualTo(old);
   assertThat(db.queryForObject("SELECT count(*) FROM email_verification_codes WHERE user_id=? AND used_at IS NOT NULL",Integer.class,id)).isEqualTo(1);
   postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",old)).andExpect(status().isBadRequest());
   postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",current)).andExpect(status().isOk());
 }
 @Test @Tag("malicious") void resendCannotResetAccountGuessingBudgetAcrossClients()throws Exception{
   var in=input();UUID id=register(in);String wrong=code(in).equals("000000")?"111111":"000000";
   for(int i=0;i<5;i++)mvc.perform(post("/api/v1/account-assistance/email-verification/confirm").with(r->{r.setRemoteAddr("192.0.2."+new java.util.Random().nextInt(200));return r;}).contentType("application/json").content(json.writeValueAsString(Map.of("email",in.get("email"),"code",wrong)))).andExpect(status().isBadRequest());
   postJson("/api/v1/account-assistance/email-verification",Map.of("email",in.get("email"))).andExpect(status().isAccepted());
   postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",code(in))).andExpect(status().isTooManyRequests()).andExpect(header().string("Retry-After","900"));
   assertThat(db.queryForObject("SELECT email_verified FROM users WHERE id=?",Boolean.class,id)).isFalse();
   var expiry=new org.springframework.transaction.support.TransactionTemplate(manager);expiry.setPropagationBehavior(3);expiry.executeWithoutResult(s->db.update("UPDATE auth_rate_windows SET expires_at=CURRENT_TIMESTAMP-INTERVAL '1 second'"));
   postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",code(in))).andExpect(status().isOk());
 }
 @Test @Tag("happy") void pendingOwnerCorrectsEmailWithReauthenticationAndSessionRevocation()throws Exception{
   var in=input();UUID id=register(in);String old=code(in),token=login(in);String next="corrected-"+UUID.randomUUID()+"@example.test";
   mvc.perform(post("/api/v1/account-assistance/pending-email").header("Authorization","Bearer "+token).contentType("application/json").content(json.writeValueAsString(Map.of("newEmail",next,"password",password,"userId",UUID.randomUUID())))).andExpect(status().isAccepted());entities.clear();
   assertThat(db.queryForObject("SELECT email FROM users WHERE id=?",String.class,id)).isEqualTo(next);
   assertThat(db.queryForObject("SELECT count(*) FROM audit_logs WHERE actor_user_id=? AND action='PENDING_EMAIL_CORRECTED'",Integer.class,id)).isEqualTo(1);
   mvc.perform(get("/api/v1/auth/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
   postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",old)).andExpect(status().isBadRequest());
   in.put("email",next);postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",next,"code",code(in))).andExpect(status().isOk());
 }
 @Test @Tag("malicious") void correctionRequiresOwnValidPendingSessionAndPassword()throws Exception{
   var in=input();UUID id=register(in);String token=login(in),next="bad-"+UUID.randomUUID()+"@example.test";
   postJson("/api/v1/account-assistance/pending-email",Map.of("newEmail",next,"password",password)).andExpect(status().isUnauthorized());
   mvc.perform(post("/api/v1/account-assistance/pending-email").header("Authorization","Bearer "+token).contentType("application/json").content(json.writeValueAsString(Map.of("newEmail",next,"password","incorrect")))).andExpect(status().isForbidden());
   assertThat(db.queryForObject("SELECT email FROM users WHERE id=?",String.class,id)).isEqualTo(in.get("email"));
 }
 @Test @Tag("confused") void unavailableCorrectionAddressDoesNotDiscloseItsOwnerOrMutateEitherAccount()throws Exception{
   var a=input();var b=input();UUID id=register(a),other=register(b);String token=login(a);
   String response=mvc.perform(post("/api/v1/account-assistance/pending-email").header("Authorization","Bearer "+token).contentType("application/json").content(json.writeValueAsString(Map.of("newEmail",b.get("email"),"password",password)))).andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
   assertThat(response).doesNotContain(other.toString(),b.get("email").toString());
   assertThat(db.queryForObject("SELECT email FROM users WHERE id=?",String.class,id)).isEqualTo(a.get("email"));
 }
 @Test @Tag("malicious") void verifiedAccountCannotUsePendingCorrectionOrRequestAnotherChallenge()throws Exception{
   var in=input();UUID id=register(in);postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",code(in))).andExpect(status().isOk());entities.clear();String token=login(in);
   postJson("/api/v1/account-assistance/email-verification",Map.of("email",in.get("email"))).andExpect(status().isAccepted());
   assertThat(db.queryForObject("SELECT count(*) FROM email_verification_codes WHERE user_id=?",Integer.class,id)).isEqualTo(1);
   mvc.perform(post("/api/v1/account-assistance/pending-email").header("Authorization","Bearer "+token).contentType("application/json").content(json.writeValueAsString(Map.of("newEmail","new@example.test","password",password)))).andExpect(status().isForbidden());
 }
 @Test @Tag("malicious") void pendingActionsAreDeniedAcrossEverySocialCategoryWithoutWrites()throws Exception{
   var in=input();register(in);String token=login(in),id="00000000-0000-0000-0000-000000000001";
   int before=db.queryForObject("SELECT (SELECT count(*) FROM posts)+(SELECT count(*) FROM comments)+(SELECT count(*) FROM messages)+(SELECT count(*) FROM follows)+(SELECT count(*) FROM post_likes)",Integer.class);
   for(String route:List.of("posts","posts/"+id+"/comments","conversations/users/"+id,"conversations/"+id+"/messages","listeners/match","listener-applications","professionals/"+id+"/availability","admin/broadcasts"))
     mvc.perform(post("/api/v1/"+route).header("Authorization","Bearer "+token).contentType("application/json").content("{}")).andExpect(status().isForbidden());
   for(String route:List.of("posts/"+id+"/like","comments/"+id+"/like","social/follows/"+id,"communities/"+id+"/membership"))
     mvc.perform(put("/api/v1/"+route).header("Authorization","Bearer "+token).contentType("application/json").content("{}")).andExpect(status().isForbidden());
   mvc.perform(get("/api/v1/profiles/other-member").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
   assertThat(db.queryForObject("SELECT (SELECT count(*) FROM posts)+(SELECT count(*) FROM comments)+(SELECT count(*) FROM messages)+(SELECT count(*) FROM follows)+(SELECT count(*) FROM post_likes)",Integer.class)).isEqualTo(before);
   for(String route:List.of("identity","profiles/me","preferences/me","policies/terms","public/resources"))mvc.perform(get("/api/v1/"+route).header("Authorization","Bearer "+token)).andExpect(status().isOk());
 }
}
