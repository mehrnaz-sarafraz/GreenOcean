package com.greenocean.backend;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import com.greenocean.backend.preference.service.UserPreferencesService;
import com.greenocean.backend.preference.dto.UpdateUserPreferencesRequest;
import tools.jackson.databind.ObjectMapper;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties={"app.privacy.support-topics-enabled=true","app.governance.optional-processing-approved=true"}) @ActiveProfiles("test") @Transactional
class PhaseGHTopicGateTest {
 @Autowired JdbcTemplate db;@Autowired UserPreferencesService preferences;@Autowired ObjectMapper json;
 @Test @Tag("creative") void reviewedTopicGateUsesStableActiveSlugsAndAllowsExplicitClear() throws Exception {
  UUID owner=db.queryForObject("INSERT INTO users(email,password_hash,status) VALUES (?,'synthetic','ACTIVE') RETURNING id",UUID.class,"gh-topics-"+UUID.randomUUID()+"@example.test");
  db.update("UPDATE users SET email_verified=TRUE WHERE id=?",owner);
  db.update("INSERT INTO account_eligibility(user_id,age_result,age_method,checked_at,residence_country) VALUES (?,'ADULT','SELF_ATTESTED',CURRENT_TIMESTAMP,'IR')",owner);
  UUID notice=db.queryForObject("INSERT INTO policy_documents(policy_key,version,title,summary,content,status,published_at,effective_at,review_status) VALUES ('support-topics-processing',900002,'Synthetic reviewed purpose','Test only','Synthetic purpose notice','PUBLISHED',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,'APPROVED') RETURNING id",UUID.class);
  db.update("INSERT INTO processing_authorizations(user_id,purpose,decision,notice_id) VALUES (?,'SUPPORT_TOPICS','GRANTED',?)",owner,notice);
  var saved=preferences.update(owner,json.readValue("{\"supportTopics\":[\"anxiety\",\"sleep\"]}",UpdateUserPreferencesRequest.class));
  assertThat(saved.supportTopics()).containsExactly("anxiety","sleep");assertThat(saved.topicCollectionEnabled()).isTrue();
  assertThatThrownBy(()->preferences.update(owner,json.readValue("{\"supportTopics\":[\"Anxiety\"]}",UpdateUserPreferencesRequest.class))).isInstanceOf(com.greenocean.backend.common.exception.PreferenceValidationException.class);
  assertThatThrownBy(()->preferences.update(owner,json.readValue("{\"supportTopics\":[\"invented\"]}",UpdateUserPreferencesRequest.class))).isInstanceOf(com.greenocean.backend.common.exception.PreferenceValidationException.class);
  assertThat(preferences.get(owner).supportTopics()).containsExactly("anxiety","sleep");
  assertThat(preferences.update(owner,json.readValue("{\"supportTopics\":[]}",UpdateUserPreferencesRequest.class)).supportTopics()).isEmpty();
 }
}
