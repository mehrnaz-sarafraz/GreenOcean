package com.greenocean.backend.assistance.service;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Fail explicitly before dispatch if live pending payloads cannot be read after a key change. */
@Component
public class DeliveryKeyReadiness implements ApplicationRunner {
    private volatile boolean ready;
    private final JdbcTemplate db;private final OutboxCipher cipher;
    public DeliveryKeyReadiness(JdbcTemplate db,OutboxCipher cipher){this.db=db;this.cipher=cipher;}
    public void run(ApplicationArguments args){
        var pending=db.queryForList("SELECT payload->>'encrypted' FROM email_outbox WHERE status IN ('PENDING','FAILED','SENDING') AND expires_at>CURRENT_TIMESTAMP AND jsonb_exists(payload,'encrypted')",String.class);
        for(String value:pending)try{cipher.decrypt(value);}catch(RuntimeException e){throw new IllegalStateException("Pending delivery key readiness failed; restore the previous key ring before starting workers");}
        ready=true;
    }
    public boolean ready(){return ready;}
}
