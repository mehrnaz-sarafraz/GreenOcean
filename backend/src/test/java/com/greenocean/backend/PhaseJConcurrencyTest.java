package com.greenocean.backend;
import org.junit.jupiter.api.*;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.test.context.ActiveProfiles;import org.springframework.jdbc.core.JdbcTemplate;
import com.greenocean.backend.preference.service.*;import com.greenocean.backend.preference.dto.UpdateUserPreferencesRequest;import tools.jackson.databind.ObjectMapper;
import java.util.*;import java.util.concurrent.*;import static org.assertj.core.api.Assertions.*;
@SpringBootTest(properties={"app.privacy.support-topics-enabled=true","app.governance.optional-processing-approved=true"}) @ActiveProfiles("test")
class PhaseJConcurrencyTest {
 @Autowired JdbcTemplate db;@Autowired PrivacyGovernanceService privacy;@Autowired UserPreferencesService preferences;@Autowired ObjectMapper json;
 @Test @Tag("malicious") void withdrawalRacingTopicSaveAlwaysLeavesNoSensitiveSelections()throws Exception{
  UUID owner=db.queryForObject("INSERT INTO users(email,password_hash,status,email_verified) VALUES (?,'synthetic','ACTIVE',TRUE) RETURNING id",UUID.class,"j-race-"+UUID.randomUUID()+"@example.test");
  db.update("INSERT INTO account_eligibility(user_id,age_result,age_method,checked_at,residence_country) VALUES (?,'ADULT','SELF_ATTESTED',CURRENT_TIMESTAMP,'IR')",owner);
  UUID notice=db.queryForObject("INSERT INTO policy_documents(policy_key,version,title,summary,content,status,published_at,effective_at,review_status) VALUES ('support-topics-processing',900003,'Synthetic race notice','QA only','Synthetic','PUBLISHED',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,'APPROVED') RETURNING id",UUID.class);
  try(var pool=Executors.newFixedThreadPool(2)){
   privacy.decide(owner,"SUPPORT_TOPICS","GRANTED",notice);var start=new CountDownLatch(1);
   var write=pool.submit(()->{start.await();try{preferences.update(owner,json.readValue("{\"supportTopics\":[\"anxiety\"]}",UpdateUserPreferencesRequest.class));return "saved";}catch(com.greenocean.backend.common.exception.PreferenceValidationException denied){return "denied";}});
   var withdraw=pool.submit(()->{start.await();privacy.decide(owner,"SUPPORT_TOPICS","WITHDRAWN",null);return true;});start.countDown();assertThat(write.get(15,TimeUnit.SECONDS)).isIn("saved","denied");assertThat(withdraw.get(15,TimeUnit.SECONDS)).isTrue();assertThat(preferences.get(owner).supportTopics()).isEmpty();assertThat(preferences.get(owner).topicCollectionEnabled()).isFalse();
  }finally{db.update("DELETE FROM processing_authorizations WHERE user_id=?",owner);db.update("DELETE FROM policy_documents WHERE id=?",notice);db.update("DELETE FROM users WHERE id=?",owner);}
 }
}
