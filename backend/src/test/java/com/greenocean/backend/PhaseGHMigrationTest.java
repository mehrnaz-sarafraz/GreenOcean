package com.greenocean.backend;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class PhaseGHMigrationTest {
 @Test @Tag("creative") void populatedEfDatabasePreservesChoicesAndConservativelyFreezesHistoricalAudiences() {
  String url=System.getenv("GREENOCEAN_TEST_DB_URL");assertThat(url).isNotBlank();String schema="qa_gh_"+UUID.randomUUID().toString().replace("-","");
  var source=new DriverManagerDataSource(url,System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER","postgres"),System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD",""));
  Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).target("39").load().migrate();
  var db=new JdbcTemplate(new DriverManagerDataSource(url+(url.contains("?")?"&":"?")+"currentSchema="+schema,System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER","postgres"),System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD","")));
  try {
   UUID publicUser=db.queryForObject("INSERT INTO users(email,password_hash,status,email_verified) VALUES ('gh-legacy@example.test','preserved','ACTIVE',TRUE) RETURNING id",UUID.class);
   db.update("INSERT INTO profiles(user_id,username,display_name,is_profile_private,avatar_url) VALUES (?,'gh_legacy','Legacy',FALSE,'https://images.example.test/retained.png')",publicUser);
   db.update("UPDATE user_preferences SET onboarding_complete=TRUE,support_topics=ARRAY['Anxiety','unmapped historical text'] WHERE user_id=?",publicUser);
   UUID oldPost=db.queryForObject("INSERT INTO posts(author_id,body,visibility) VALUES (?,'Historical private intent unknown','PUBLIC') RETURNING id",UUID.class,publicUser);
   UUID followerPost=db.queryForObject("INSERT INTO posts(author_id,body,visibility) VALUES (?,'Followers original','FOLLOWERS') RETURNING id",UUID.class,publicUser);
   var migration=Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).load();migration.migrate();migration.validate();
   assertThat(db.queryForObject("SELECT visibility FROM posts WHERE id=?",String.class,oldPost)).isEqualTo("FOLLOWERS");
   assertThat(db.queryForObject("SELECT visibility FROM posts WHERE id=?",String.class,followerPost)).isEqualTo("FOLLOWERS");
   assertThat(db.queryForMap("SELECT is_profile_private,avatar_url FROM profiles WHERE user_id=?",publicUser)).containsEntry("is_profile_private",false).containsEntry("avatar_url","https://images.example.test/retained.png");
   assertThat(db.queryForObject("SELECT onboarding_status FROM user_preferences WHERE user_id=?",String.class,publicUser)).isEqualTo("LEGACY_UNKNOWN");
   assertThat(db.queryForObject("SELECT 'unmapped historical text'=ANY(legacy_support_topics) FROM user_preferences WHERE user_id=?",Boolean.class,publicUser)).isTrue();
   assertThat(db.queryForObject("SELECT 'anxiety'=ANY(support_topics) FROM user_preferences WHERE user_id=?",Boolean.class,publicUser)).isTrue();
   db.update("UPDATE profiles SET is_profile_private=TRUE WHERE user_id=?",publicUser);db.update("UPDATE profiles SET is_profile_private=FALSE WHERE user_id=?",publicUser);
   assertThat(db.queryForObject("SELECT can_read_social_post(?,?)",Boolean.class,oldPost,UUID.randomUUID())).isFalse();
   assertThat(db.queryForObject("SELECT password_hash FROM users WHERE id=?",String.class,publicUser)).isEqualTo("preserved");
  } finally {db.execute("DROP SCHEMA "+schema+" CASCADE");}
 }
}
