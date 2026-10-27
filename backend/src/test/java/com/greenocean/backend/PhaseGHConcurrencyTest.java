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
