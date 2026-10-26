package com.greenocean.backend;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class PhaseAMigrationTest {
    @Test @Tag("creative") void upgradesLegacyDatabaseWithoutInventingVerificationOrDeletingUsers() {
        String url=System.getenv("GREENOCEAN_TEST_DB_URL");
        assertThat(url).as("An explicitly isolated QA database is mandatory").isNotBlank();
        var source=new DriverManagerDataSource(url,System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER","postgres"),System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD",""));
        String schema="qa_migration_"+UUID.randomUUID().toString().replace("-","");
        var db=new JdbcTemplate(source);
        var old=Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).target("30").load();
        old.migrate();
        db=new JdbcTemplate(new DriverManagerDataSource(url+(url.contains("?")?"&":"?")+"currentSchema="+schema,
            System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER","postgres"),System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD","")));
        try {
            db.update("INSERT INTO "+schema+".users(email,password_hash,status,email_verified) VALUES ('legacy@example.test','synthetic-hash','ACTIVE',FALSE)");
            var upgraded=Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).load();upgraded.migrate();upgraded.validate();
            assertThat(db.queryForObject("SELECT COUNT(*) FROM "+schema+".users WHERE email='legacy@example.test' AND status='ACTIVE' AND NOT email_verified AND NOT phase_a_enrolled",Integer.class)).isEqualTo(1);
            assertThat(db.queryForObject("SELECT COUNT(*) FROM "+schema+".geography_countries",Integer.class)).isGreaterThan(240);
            assertThat(db.queryForObject("SELECT COUNT(*) FROM "+schema+".regional_services",Integer.class)).isZero();
        } finally {db.execute("DROP SCHEMA "+schema+" CASCADE");}
    }
}
