package com.greenocean.backend;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class PhaseDMigrationTest extends MigrationTestSupport {
 @Test @Tag("creative") void populatedV35UpgradesWithoutChangingAccountsOrLosingQueuedCiphertext(){
   String url=System.getenv("GREENOCEAN_TEST_DB_URL");assertThat(url).isNotBlank();
   String schema="qa_d_"+UUID.randomUUID().toString().replace("-","");
   var source=new DriverManagerDataSource(url,System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER","postgres"),System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD",""));
   var old=Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).target("35").load();old.migrate();
   var db=new JdbcTemplate(new DriverManagerDataSource(url+(url.contains("?")?"&":"?")+"currentSchema="+schema,System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER","postgres"),System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD","")));
   try{
     UUID id=db.queryForObject("INSERT INTO users(email,password_hash,status,email_verified) VALUES ('d-legacy@example.test','synthetic-hash','ACTIVE',TRUE) RETURNING id",UUID.class);
     db.update("INSERT INTO email_verification_codes(user_id,code_hash,expires_at) VALUES (?,'synthetic-old-hmac',CURRENT_TIMESTAMP+INTERVAL '15 minutes'),(?,'synthetic-newer-hmac',CURRENT_TIMESTAMP+INTERVAL '15 minutes')",id,id);
     db.update("INSERT INTO email_outbox(recipient,template_key,payload) VALUES ('d-legacy@example.test','email-verification','{\"encrypted\":\"synthetic-preserved-ciphertext\"}')");
     var latest=Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).load();latest.migrate();latest.validate();
     assertThat(db.queryForMap("SELECT status,email_verified,password_hash FROM users WHERE id=?",id)).containsEntry("status","ACTIVE").containsEntry("email_verified",true).containsEntry("password_hash","synthetic-hash");
     assertThat(db.queryForObject("SELECT payload->>'encrypted' FROM email_outbox WHERE recipient='d-legacy@example.test'",String.class)).isEqualTo("synthetic-preserved-ciphertext");
     assertThat(db.queryForObject("SELECT count(*) FROM email_verification_codes WHERE user_id=? AND used_at IS NULL",Integer.class,id)).isEqualTo(1);
     assertThat(db.queryForObject("SELECT owner_user_id FROM email_outbox WHERE recipient='d-legacy@example.test'",UUID.class)).isEqualTo(id);
   }finally{db.execute("DROP SCHEMA "+schema+" CASCADE");}
 }
}
