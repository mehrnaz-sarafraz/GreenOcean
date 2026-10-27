package com.greenocean.backend;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.UUID;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.*;
class PhaseIMigrationTest extends MigrationTestSupport {
 @Test void populatedGhHistoryIsPreservedAndFailedForwardMigrationRecoversTransactionally()throws Exception{
  String url=System.getenv("GREENOCEAN_TEST_DB_URL"),schema="qa_i_"+UUID.randomUUID().toString().replace("-","");
  var source=new DriverManagerDataSource(url,System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER","postgres"),System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD",""));
  Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).target("40").load().migrate();
  var scoped=new DriverManagerDataSource(url+(url.contains("?")?"&":"?")+"currentSchema="+schema+",public",System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER","postgres"),System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD",""));var db=new JdbcTemplate(scoped);
  Path failure=Files.createTempDirectory("greenocean-i-migration-");
  try{
   UUID user=db.queryForObject("INSERT INTO users(email,password_hash,status) VALUES ('i-legacy@example.test','preserved','ACTIVE') RETURNING id",UUID.class);
   db.update("INSERT INTO profiles(user_id,username,display_name) VALUES (?,'i_legacy','Legacy')",user);
   db.update("INSERT INTO listener_profiles(user_id,listening_style,online) VALUES (?,'Preserved legacy profile',TRUE)",user);
   UUID chat=db.queryForObject("INSERT INTO conversations(conversation_kind,title,service_kind) VALUES ('DIRECT','Legacy listener','LISTENER') RETURNING id",UUID.class);
   db.update("INSERT INTO conversation_members(conversation_id,user_id) VALUES (?,?)",chat,user);db.update("INSERT INTO messages(conversation_id,sender_id,message_kind,body) VALUES (?,?,'USER','Preserved historical message')",chat,user);
   Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).load().migrate();
   assertThat(db.queryForObject("SELECT body FROM messages WHERE conversation_id=?",String.class,chat)).isEqualTo("Preserved historical message");
   assertThat(db.queryForObject("SELECT count(*) FROM live_support_requests",Integer.class)).isZero();assertThat(db.queryForObject("SELECT count(*) FROM listener_live_authorizations",Integer.class)).isZero();
   assertThat(db.queryForObject("SELECT listening_style FROM listener_profiles WHERE user_id=?",String.class,user)).isEqualTo("Preserved legacy profile");
   Files.writeString(failure.resolve("V999999__synthetic_recovery.sql"),"CREATE TABLE synthetic_partial(id INT); DO $$ BEGIN RAISE EXCEPTION 'synthetic failure'; END $$;");
   var broken=Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).locations("classpath:db/migration","filesystem:"+failure).load();assertThatThrownBy(broken::migrate).isInstanceOf(Exception.class);
   assertThat(db.queryForObject("SELECT to_regclass(?) IS NULL",Boolean.class,schema+".synthetic_partial")).isTrue();
   Files.writeString(failure.resolve("V999999__synthetic_recovery.sql"),"CREATE TABLE synthetic_recovered(id INT);");broken.migrate();broken.validate();
   assertThat(db.queryForObject("SELECT to_regclass(?) IS NOT NULL",Boolean.class,schema+".synthetic_recovered")).isTrue();
  }finally{db.execute("DROP SCHEMA "+schema+" CASCADE");Files.deleteIfExists(failure.resolve("V999999__synthetic_recovery.sql"));Files.deleteIfExists(failure);}
 }
}
