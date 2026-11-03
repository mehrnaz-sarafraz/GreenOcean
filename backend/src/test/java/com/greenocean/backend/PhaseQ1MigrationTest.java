package com.greenocean.backend;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class PhaseQ1MigrationTest extends MigrationTestSupport {
 @Test void populatedP5UpgradePreservesSourceAndCreatesNoStaffOrFalseApproval(){String url=System.getenv("GREENOCEAN_TEST_DB_URL"),schema="qa_q1_"+UUID.randomUUID().toString().replace("-","");var source=new DriverManagerDataSource(url,System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER","postgres"),System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD",""));Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).target("83").load().migrate();var db=new JdbcTemplate(new DriverManagerDataSource(url+(url.contains("?")?"&":"?")+"currentSchema="+schema+",public",System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER","postgres"),System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD","")));
  try{UUID u=db.queryForObject("INSERT INTO users(email,password_hash,status,email_verified) VALUES('q1-upgrade@example.test','synthetic-only','ACTIVE',TRUE) RETURNING id",UUID.class);db.update("INSERT INTO support_requests(user_id,email,message) VALUES(?,'q1-upgrade@example.test','SYNTHETIC_UNCOPIED_PRIVATE_SOURCE')",u);var before=db.queryForList("SELECT id,user_id,email,message,status FROM support_requests");var flyway=Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).load();flyway.migrate();flyway.validate();assertThat(db.queryForList("SELECT id,user_id,email,message,status FROM support_requests")).isEqualTo(before);assertThat(db.queryForObject("SELECT count(*) FROM trust_cases",Integer.class)).isZero();assertThat(db.queryForObject("SELECT count(*) FROM trust_coverage",Integer.class)).isZero();assertThat(db.queryForObject("SELECT count(*) FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE r.name LIKE 'TRUST_%'",Integer.class)).isZero();assertThat(db.queryForObject("SELECT count(*) FROM information_schema.columns WHERE table_schema=? AND table_name IN('trust_cases','trust_case_events') AND column_name IN('body','description','email','author_id','clinical_note')",Integer.class,schema)).isZero();
  }finally{db.execute("DROP SCHEMA "+schema+" CASCADE");}
 }
}
