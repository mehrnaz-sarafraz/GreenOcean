package com.greenocean.backend;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import com.greenocean.backend.preference.service.UserPreferencesService;
import com.greenocean.backend.preference.dto.UpdateUserPreferencesRequest;
import tools.jackson.databind.ObjectMapper;
import java.util.*;import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test")
class PhaseGHConcurrencyTest {
 @Autowired JdbcTemplate db;@Autowired UserPreferencesService preferences;@Autowired ObjectMapper json;
 @Autowired com.greenocean.backend.auth.service.RegistrationService registration;
 @Autowired org.springframework.transaction.PlatformTransactionManager manager;
 com.greenocean.backend.auth.dto.RegisterRequest input(String email){return new com.greenocean.backend.auth.dto.RegisterRequest(email,"Synthetic GH registration phrase",null,null,null,"IR",null,null,true,1);}
 @Test @Tag("creative") void simultaneousDistinctRegistrationsKeepUniqueUsernamesAndAllowDuplicateDisplayNames() throws Exception {
  String a="gh-identity-"+UUID.randomUUID()+"@example.test",b="gh-identity-"+UUID.randomUUID()+"@example.test";
  try(var pool=Executors.newFixedThreadPool(2)) {
   var start=new CountDownLatch(1);
   var first=pool.submit(()->{start.await();return registration.register(input(a));});var second=pool.submit(()->{start.await();return registration.register(input(b));});start.countDown();first.get(20,TimeUnit.SECONDS);second.get(20,TimeUnit.SECONDS);
   var names=db.queryForList("SELECT username FROM profiles WHERE user_id IN (SELECT id FROM users WHERE email IN (?,?))",String.class,a,b);
   assertThat(names).hasSize(2).doesNotHaveDuplicates().allMatch(name->name.startsWith("member_"));
   assertThat(db.queryForList("SELECT DISTINCT display_name FROM profiles WHERE user_id IN (SELECT id FROM users WHERE email IN (?,?))",String.class,a,b)).containsExactly("New member");
  }finally{db.update("DELETE FROM email_outbox WHERE recipient IN (?,?)",a,b);db.update("DELETE FROM users WHERE email IN (?,?)",a,b);}
 }
 @Test @Tag("confused") void failedRegistrationTransactionLeavesNoPartialProfileEligibilityOrEmail() {
  String email="gh-rollback-"+UUID.randomUUID()+"@example.test";
  assertThatThrownBy(()->new org.springframework.transaction.support.TransactionTemplate(manager).executeWithoutResult(status->{registration.register(input(email));throw new IllegalStateException("Synthetic transaction failure");})).isInstanceOf(IllegalStateException.class);
  assertThat(db.queryForObject("SELECT count(*) FROM users WHERE email=?",Integer.class,email)).isZero();
  assertThat(db.queryForObject("SELECT count(*) FROM email_outbox WHERE recipient=?",Integer.class,email)).isZero();
 }
 @Test @Tag("creative") void concurrentIndependentPreferencePatchesPreserveBothConfirmedChanges() throws Exception {
  UUID owner=db.queryForObject("INSERT INTO users(email,password_hash,status) VALUES (?,'synthetic','ACTIVE') RETURNING id",UUID.class,"gh-race-"+UUID.randomUUID()+"@example.test");
  try(var pool=Executors.newFixedThreadPool(2)) {
   var start=new CountDownLatch(1);
   var a=pool.submit(()->{start.await();preferences.update(owner,json.readValue("{\"allowMessageRequests\":true}",UpdateUserPreferencesRequest.class));return true;});
   var b=pool.submit(()->{start.await();preferences.update(owner,json.readValue("{\"blurSensitiveContent\":false}",UpdateUserPreferencesRequest.class));return true;});
   start.countDown();assertThat(a.get(15,TimeUnit.SECONDS)).isTrue();assertThat(b.get(15,TimeUnit.SECONDS)).isTrue();
   var saved=preferences.get(owner);assertThat(saved.allowMessageRequests()).isTrue();assertThat(saved.blurSensitiveContent()).isFalse();assertThat(saved.strongerContentControls()).isTrue();
  }finally{db.update("DELETE FROM users WHERE id=?",owner);}
 }
}
