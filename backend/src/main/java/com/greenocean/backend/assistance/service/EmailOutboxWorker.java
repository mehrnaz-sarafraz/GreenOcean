package com.greenocean.backend.assistance.service;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailOutboxWorker {
    private final JdbcTemplate db; private final OutboxCipher cipher; private final MailTransport mail;
    private final boolean enabled; private final String from;
    public EmailOutboxWorker(JdbcTemplate db,OutboxCipher cipher,MailTransport mail,
      @Value("${greenocean.mail.enabled:false}") boolean enabled,@Value("${greenocean.mail.from:no-reply@greenocean.invalid}") String from) {
        this.db=db;this.cipher=cipher;this.mail=mail;this.enabled=enabled;this.from=from;
    }
    @Scheduled(fixedDelayString="${greenocean.mail.poll-ms:5000}") @Transactional
    public void deliver() {
        db.update("UPDATE email_outbox SET payload='{}',status='FAILED' WHERE expires_at<CURRENT_TIMESTAMP AND status IN ('PENDING','FAILED')");
        db.update("DELETE FROM auth_rate_windows WHERE expires_at<CURRENT_TIMESTAMP - INTERVAL '1 day'");
        db.update("DELETE FROM retired_refresh_tokens WHERE expires_at<CURRENT_TIMESTAMP");
        if(!enabled) return;
        var rows=db.queryForList("""
          SELECT id,recipient,template_key,payload->>'encrypted' AS encrypted FROM email_outbox
          WHERE status IN ('PENDING','FAILED') AND attempts<5 AND expires_at>CURRENT_TIMESTAMP
          AND jsonb_exists(payload,'encrypted') ORDER BY created_at LIMIT 2 FOR UPDATE SKIP LOCKED
          """);
        for(var row:rows) {
            try {
                mail.send((java.util.UUID)row.get("id"),(String)row.get("recipient"),(String)row.get("template_key"),cipher.decrypt((String)row.get("encrypted")));
                db.update("UPDATE email_outbox SET status='SENT',sent_at=CURRENT_TIMESTAMP,payload='{}',attempts=attempts+1 WHERE id=?",row.get("id"));
            } catch(Exception ignored) {
                // Never log mail addresses, provider errors, tokens, or payloads.
                db.update("UPDATE email_outbox SET status='FAILED',attempts=attempts+1 WHERE id=?",row.get("id"));
            }
        }
    }
}
