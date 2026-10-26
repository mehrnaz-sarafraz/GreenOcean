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
    private final int maximumAttempts,batchSize,leaseSeconds;
    private final DeliveryKeyReadiness readiness;
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(EmailOutboxWorker.class);
    public EmailOutboxWorker(JdbcTemplate db,OutboxCipher cipher,MailTransport mail,
      boolean enabled,String from){this(db,cipher,mail,enabled,from,5,2,30,null);}
    @org.springframework.beans.factory.annotation.Autowired
    public EmailOutboxWorker(JdbcTemplate db,OutboxCipher cipher,MailTransport mail,
      @Value("${greenocean.mail.enabled:false}") boolean enabled,@Value("${greenocean.mail.from:no-reply@greenocean.invalid}") String from,
      @Value("${greenocean.mail.max-attempts:5}") int maximumAttempts,@Value("${greenocean.mail.batch-size:10}") int batchSize,
      @Value("${greenocean.mail.lease-seconds:30}") int leaseSeconds,DeliveryKeyReadiness readiness) {
        if(maximumAttempts<1||maximumAttempts>20||batchSize<1||batchSize>100||leaseSeconds<20)throw new IllegalArgumentException("Invalid mail worker limits");
        this.db=db;this.cipher=cipher;this.mail=mail;this.enabled=enabled;this.from=from;
        this.maximumAttempts=maximumAttempts;this.batchSize=batchSize;this.leaseSeconds=leaseSeconds;this.readiness=readiness;
    }
    @Scheduled(fixedDelayString="${greenocean.mail.poll-ms:5000}")
    public void deliver() {
        if(readiness!=null&&!readiness.ready())return;
        db.update("UPDATE email_outbox SET payload='{}',status='DEAD',outcome_code='EXPIRED',lease_token=NULL,lease_until=NULL WHERE expires_at<=CURRENT_TIMESTAMP AND status IN ('PENDING','FAILED','SENDING')");
        db.update("UPDATE email_outbox SET payload='{}',status='DEAD',outcome_code='RETRY_EXHAUSTED',lease_token=NULL,lease_until=NULL WHERE attempts>=? AND (status IN ('PENDING','FAILED') OR (status='SENDING' AND lease_until<=CURRENT_TIMESTAMP))",maximumAttempts);
        db.update("DELETE FROM auth_rate_windows WHERE expires_at<CURRENT_TIMESTAMP - INTERVAL '1 day'");
        db.update("DELETE FROM retired_refresh_tokens WHERE expires_at<CURRENT_TIMESTAMP");
        if(!enabled) return;
        for(int slot=0;slot<batchSize;slot++) {
        var lease=java.util.UUID.randomUUID();
        var rows=db.queryForList("""
          WITH due AS (
            SELECT id FROM email_outbox WHERE attempts<? AND expires_at>CURRENT_TIMESTAMP
            AND ((status IN ('PENDING','FAILED') AND next_attempt_at<=CURRENT_TIMESTAMP)
                 OR (status='SENDING' AND lease_until<=CURRENT_TIMESTAMP))
            ORDER BY created_at LIMIT ? FOR UPDATE SKIP LOCKED
          ) UPDATE email_outbox o SET status='SENDING',attempts=attempts+1,lease_token=?,
              lease_until=CURRENT_TIMESTAMP+(? * INTERVAL '1 second')
            FROM due WHERE o.id=due.id RETURNING o.id,o.recipient,o.template_key,o.attempts,floor(extract(epoch from o.expires_at))::bigint AS expiry_epoch,o.payload->>'encrypted' AS encrypted
          """,maximumAttempts,1,lease,leaseSeconds);
        if(rows.isEmpty())break;
        for(var row:rows) {
            var id=(java.util.UUID)row.get("id");
            try {
                // Claim commits before network I/O. A resend/change can cancel a claimed obsolete message.
                boolean current=Boolean.TRUE.equals(db.queryForObject("""
                    SELECT EXISTS(SELECT 1 FROM email_outbox o WHERE o.id=? AND o.status='SENDING' AND o.lease_token=?
                      AND o.expires_at>CURRENT_TIMESTAMP AND (o.template_key<>'email-verification' OR EXISTS(
                        SELECT 1 FROM email_verification_codes c JOIN users u ON u.id=c.user_id
                        WHERE c.id=o.verification_challenge_id AND c.used_at IS NULL AND c.expires_at>CURRENT_TIMESTAMP
                        AND u.status IN ('PENDING','ACTIVE') AND NOT u.email_verified AND u.email=o.recipient)))
                    """,Boolean.class,id,lease));
                if(!current){finish(id,lease,"CANCELLED","SUPERSEDED",null);continue;}
                String template=(String)row.get("template_key");
                if(!java.util.Set.of("email-verification","password-reset").contains(template)){
                    finish(id,lease,"DEAD","INVALID_TEMPLATE",null);continue;
                }
                String plaintext;
                try {plaintext=cipher.decrypt((String)row.get("encrypted"));}
                catch(RuntimeException e){finish(id,lease,"DEAD","KEY_OR_PAYLOAD_ERROR",null);continue;}
                var receipt=mail.submit(id,from,(String)row.get("recipient"),template,plaintext,"en",java.time.Instant.ofEpochSecond(((Number)row.get("expiry_epoch")).longValue()));
                finish(id,lease,"SENT","PROVIDER_ACCEPTED",receipt.providerReference());
            } catch(Exception failure) {
                boolean permanent=failure instanceof MailDeliveryFailure e && e.kind()==MailDeliveryFailure.Kind.PERMANENT;
                boolean exhausted=((Number)row.get("attempts")).intValue()>=maximumAttempts;
                if(permanent||exhausted)finish(id,lease,"DEAD",permanent?"PERMANENT_FAILURE":"RETRY_EXHAUSTED",null);
                else {
                    long base=Math.min(300,15L << Math.min(5,((Number)row.get("attempts")).intValue()-1));
                    long delay=base+java.util.concurrent.ThreadLocalRandom.current().nextLong(Math.max(1,base/4));
                    String outcome=failure instanceof MailDeliveryFailure e&&e.kind()==MailDeliveryFailure.Kind.UNCERTAIN?"ACCEPTANCE_UNCERTAIN":"RETRYABLE_FAILURE";
                    db.update("UPDATE email_outbox SET status='FAILED',outcome_code=?,next_attempt_at=CURRENT_TIMESTAMP+(? * INTERVAL '1 second'),lease_token=NULL,lease_until=NULL WHERE id=? AND status='SENDING' AND lease_token=?",outcome,delay,id,lease);
                    log.warn("mail_delivery outcome={}",outcome);
                }
            }
        }
        }
    }
    private void finish(java.util.UUID id,java.util.UUID lease,String status,String outcome,String reference) {
        int changed=db.update("""
            UPDATE email_outbox SET status=?,outcome_code=?,payload='{}',lease_token=NULL,lease_until=NULL,
              sent_at=CASE WHEN ?='SENT' THEN CURRENT_TIMESTAMP ELSE sent_at END,provider_reference=?,
              delivery_state=CASE WHEN ?='SENT' THEN 'ACCEPTED' ELSE delivery_state END
            WHERE id=? AND status='SENDING' AND lease_token=?
            """,status,outcome,status,reference,status,id,lease);
        if(changed>0)log.info("mail_delivery outcome={}",outcome);
    }
    @Scheduled(fixedDelayString="${greenocean.mail.metrics-poll-ms:60000}")
    public void reportQueue() {
        if(!enabled||(readiness!=null&&!readiness.ready()))return;
        var counts=db.queryForMap("""
          SELECT count(*) FILTER(WHERE status='PENDING') AS pending,count(*) FILTER(WHERE status='FAILED') AS retry,
            count(*) FILTER(WHERE status='DEAD') AS terminal,count(*) FILTER(WHERE status='SENDING' AND lease_until<CURRENT_TIMESTAMP) AS stuck,
            count(*) FILTER(WHERE delivery_state='BOUNCED') AS bounced FROM email_outbox
          """);
        log.info("mail_queue pending={} retry={} terminal={} stuck={} bounced={}",counts.get("pending"),counts.get("retry"),counts.get("terminal"),counts.get("stuck"),counts.get("bounced"));
    }
}
