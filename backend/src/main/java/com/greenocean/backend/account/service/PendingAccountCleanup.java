package com.greenocean.backend.account.service;

import com.greenocean.backend.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Duration;
import java.util.UUID;

/** Seven days since last authorized verification activity, as approved for Phase D.
 * Existing erasure anonymizes the account; no blind FK cascade or ACTIVE/legacy deletion.
 */
@Service
public class PendingAccountCleanup {
    private final JdbcTemplate db;private final AccountRepository accounts;private final TransactionTemplate tx;
    private final boolean enabled,dryRun,paused;private final Duration retention;private final int batch;
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(PendingAccountCleanup.class);
    public PendingAccountCleanup(JdbcTemplate db,AccountRepository accounts,PlatformTransactionManager manager,
      @Value("${greenocean.pending-cleanup.enabled:false}") boolean enabled,@Value("${greenocean.pending-cleanup.dry-run:true}") boolean dryRun,
      @Value("${greenocean.pending-cleanup.paused:false}") boolean paused,@Value("${greenocean.pending-cleanup.retention:P7D}") Duration retention,
      @Value("${greenocean.pending-cleanup.batch-size:50}") int batch) {
        if(retention.isZero()||retention.isNegative()||batch<1||batch>1000)throw new IllegalArgumentException("Invalid pending retention configuration");
        this.db=db;this.accounts=accounts;this.tx=new TransactionTemplate(manager);this.enabled=enabled;this.dryRun=dryRun;this.paused=paused;this.retention=retention;this.batch=batch;
    }
    private static final String ELIGIBLE="""
        FROM users u WHERE u.status='PENDING' AND NOT u.email_verified AND u.phase_a_enrolled
        AND GREATEST(u.created_at,COALESCE(u.verification_activity_at,u.created_at),COALESCE(u.last_login_at,u.created_at))<=CURRENT_TIMESTAMP-(? * INTERVAL '1 second')
        AND NOT EXISTS(SELECT 1 FROM password_reset_tokens r WHERE r.user_id=u.id AND r.used_at IS NULL AND r.expires_at>CURRENT_TIMESTAMP)
        AND (SELECT o.status='SENT' AND o.delivery_state IN ('ACCEPTED','DELIVERED') FROM email_outbox o
             WHERE o.owner_user_id=u.id AND o.template_key='email-verification' AND o.status<>'CANCELLED'
             ORDER BY o.created_at DESC,o.id DESC LIMIT 1) IS TRUE
        AND NOT EXISTS(SELECT 1 FROM posts p WHERE p.author_id=u.id)
        AND NOT EXISTS(SELECT 1 FROM comments c WHERE c.author_id=u.id)
        AND NOT EXISTS(SELECT 1 FROM messages m WHERE m.sender_id=u.id)
        AND NOT EXISTS(SELECT 1 FROM professional_profiles p WHERE p.user_id=u.id)
        AND NOT EXISTS(SELECT 1 FROM professional_verifications p WHERE p.professional_user_id=u.id)
        AND NOT EXISTS(SELECT 1 FROM listener_applications p WHERE p.user_id=u.id)
        AND NOT EXISTS(SELECT 1 FROM reports r WHERE r.reporter_id=u.id)
        """;
    public record Result(int candidates,int erased,int failures,boolean paused) {}
    @Scheduled(fixedDelayString="${greenocean.pending-cleanup.poll-ms:3600000}")
    public void scheduled(){if(enabled)run();}
    public Result run() {
        if(!enabled||paused)return new Result(0,0,0,true);
        if(dryRun){int count=db.queryForList("SELECT u.id "+ELIGIBLE+" ORDER BY u.created_at LIMIT ?",UUID.class,retention.toSeconds(),batch).size();log.info("pending_cleanup dry_run=true candidates={}",count);return new Result(count,0,0,false);}
        int erased=0,failures=0;
        for(int n=0;n<batch;n++) {
            try {
                Boolean processed=tx.execute(status->{
                    var ids=db.queryForList("SELECT u.id "+ELIGIBLE+" ORDER BY u.created_at LIMIT 1 FOR UPDATE OF u SKIP LOCKED",UUID.class,retention.toSeconds());
                    if(ids.isEmpty())return false;
                    var id=ids.getFirst();accounts.erase(id);
                    db.update("DELETE FROM user_sessions WHERE user_id=?",id);
                    db.update("UPDATE users SET verification_activity_at=NULL,last_login_at=NULL WHERE id=?",id);
                    return true;
                });
                if(!Boolean.TRUE.equals(processed))break;erased++;
            }catch(RuntimeException failure){failures++;break;}
        }
        log.info("pending_cleanup erased={} failures={}",erased,failures);
        return new Result(erased,erased,failures,false);
    }
}
