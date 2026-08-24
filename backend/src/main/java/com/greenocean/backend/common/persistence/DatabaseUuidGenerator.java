package com.greenocean.backend.common.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DatabaseUuidGenerator {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseUuidGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UUID nextUuid() {
        return jdbcTemplate.queryForObject("SELECT uuidv7()", UUID.class);
    }
}
