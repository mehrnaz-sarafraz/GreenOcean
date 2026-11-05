package com.greenocean.backend;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class PhaseQ5MigrationTest extends MigrationTestSupport {
    @Test void populatedQ4UpgradeIsAtomicAndDoesNotSeedPilotApproval(){
        String url=System.getenv("GREENOCEAN_TEST_DB_URL"),schema="qa_q5_upgrade_"+UUID.randomUUID().toString().replace("-","");
        String username=System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER","postgres");
        String password=System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD","");
        var source=new DriverManagerDataSource(url,username,password);
        Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).target("88").load().migrate();
        var db=new JdbcTemplate(new DriverManagerDataSource(url+(url.contains("?")?"&":"?")+"currentSchema="+schema+",public",username,password));
        try{
            UUID who=db.queryForObject("INSERT INTO users(email,password_hash,status,email_verified) VALUES('q5-upgrade@example.test','synthetic-only','ACTIVE',TRUE) RETURNING id",UUID.class);
            UUID request=db.queryForObject("INSERT INTO support_requests(user_id,email,message) VALUES(?,'q5-upgrade@example.test','SYNTHETIC_Q4_CONTENT_UNCHANGED') RETURNING id",UUID.class,who);
            db.execute("CREATE TABLE pilot_evidence_reviews(synthetic_conflict INTEGER)");
            assertThatThrownBy(()->Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).load().migrate()).isInstanceOf(Exception.class);
            assertThat(db.queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_schema=? AND table_name='pilot_assessments'",Integer.class,schema)).isZero();
            db.execute("DROP TABLE pilot_evidence_reviews");
            var flyway=Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).load();flyway.migrate();flyway.validate();
            for(String table:List.of("pilot_assessments","pilot_evidence_reviews","pilot_owner_approvals","pilot_participants"))
                assertThat(db.queryForObject("SELECT count(*) FROM "+table,Integer.class)).isZero();
            assertThat(db.queryForObject("SELECT enforcement_required FROM pilot_rollout_policy WHERE id=TRUE",Boolean.class)).isFalse();
            assertThat(db.queryForObject("SELECT status FROM policy_documents WHERE policy_key='pilot-participation' AND version=1",String.class)).isEqualTo("DRAFT");
            assertThat(db.queryForObject("SELECT message FROM support_requests WHERE id=?",String.class,request)).isEqualTo("SYNTHETIC_Q4_CONTENT_UNCHANGED");
        }finally{db.execute("DROP SCHEMA "+schema+" CASCADE");}
    }
}
