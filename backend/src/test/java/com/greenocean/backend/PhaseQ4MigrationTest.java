package com.greenocean.backend;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class PhaseQ4MigrationTest extends MigrationTestSupport {
    @Test void populatedQ3UpgradeIsAtomicAndDoesNotSeedRecoveryApproval(){
        String url=System.getenv("GREENOCEAN_TEST_DB_URL"),schema="qa_q4_upgrade_"+UUID.randomUUID().toString().replace("-","");
        String username=System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER","postgres");
        String password=System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD","");
        var source=new DriverManagerDataSource(url,username,password);
        Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).target("87").load().migrate();
        var db=new JdbcTemplate(new DriverManagerDataSource(url+(url.contains("?")?"&":"?")+"currentSchema="+schema+",public",username,password));
        try{
            UUID who=db.queryForObject("INSERT INTO users(email,password_hash,status,email_verified) VALUES('q4-upgrade@example.test','synthetic-only','ACTIVE',TRUE) RETURNING id",UUID.class);
            UUID request=db.queryForObject("INSERT INTO support_requests(user_id,email,message) VALUES(?,'q4-upgrade@example.test','SYNTHETIC_Q3_CONTENT_UNCHANGED') RETURNING id",UUID.class,who);
            db.execute("CREATE TABLE recovery_replay_reviews(synthetic_conflict INTEGER)");
            assertThatThrownBy(()->Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).load().migrate()).isInstanceOf(Exception.class);
            assertThat(db.queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_schema=? AND table_name='recovery_replay_runs'",Integer.class,schema)).isZero();
            db.execute("DROP TABLE recovery_replay_reviews");
            var flyway=Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).load();flyway.migrate();flyway.validate();
            for(String table:List.of("recovery_replay_runs","recovery_replay_reviews","recovery_replay_subjects","recovery_replay_stops"))
                assertThat(db.queryForObject("SELECT count(*) FROM "+table,Integer.class)).isZero();
            assertThat(db.queryForObject("SELECT message FROM support_requests WHERE id=?",String.class,request)).isEqualTo("SYNTHETIC_Q3_CONTENT_UNCHANGED");
        }finally{db.execute("DROP SCHEMA "+schema+" CASCADE");}
    }
}
