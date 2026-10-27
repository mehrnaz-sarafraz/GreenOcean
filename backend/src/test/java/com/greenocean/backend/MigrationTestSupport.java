package com.greenocean.backend;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** Keeps historical schema tests independent of which test class runs first. */
abstract class MigrationTestSupport {
    @BeforeAll
    static void initializeApplicationSchema() {
        String url = System.getenv("GREENOCEAN_TEST_DB_URL");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("An explicitly isolated QA database is mandatory");
        }
        var source = new DriverManagerDataSource(url,
                System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER", "postgres"),
                System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD", ""));
        // V0 creates public.uuidv7 even when migrating an isolated history schema.
        // Initialize the application schema first so that this global fallback
        // cannot make a fresh public schema nonempty without Flyway history.
        // Do not baseline an existing schema or change any applied migration.
        Flyway.configure().dataSource(source).load().migrate();
    }
}
