package com.greenocean.backend.account.service;
import com.greenocean.backend.professional.service.ProfessionalDocumentStorage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountErasureWorker {
    private final JdbcTemplate db;private final ProfessionalDocumentStorage storage;
    public AccountErasureWorker(JdbcTemplate db,ProfessionalDocumentStorage storage) {this.db=db;this.storage=storage;}
    @Scheduled(fixedDelayString="${greenocean.erasure.poll-ms:60000}") @Transactional
    public void eraseFiles() {
        var rows=db.queryForList("SELECT user_id,storage_key FROM account_file_erasures WHERE completed_at IS NULL ORDER BY attempts LIMIT 20 FOR UPDATE SKIP LOCKED");
        for(var row:rows) {
            try {
                storage.delete((String)row.get("storage_key"));
                db.update("UPDATE account_file_erasures SET completed_at=CURRENT_TIMESTAMP,attempts=attempts+1 WHERE user_id=? AND storage_key=?",row.get("user_id"),row.get("storage_key"));
            } catch(Exception ignored) {
                db.update("UPDATE account_file_erasures SET attempts=attempts+1 WHERE user_id=? AND storage_key=?",row.get("user_id"),row.get("storage_key"));
                db.update("UPDATE account_deletions SET status='FAILED_RETRYABLE' WHERE user_id=?",row.get("user_id"));
            }
        }
        db.update("""
          UPDATE account_deletions SET status='COMPLETED',completed_at=CURRENT_TIMESTAMP
          WHERE status IN ('PROCESSING','FAILED_RETRYABLE') AND NOT EXISTS
          (SELECT 1 FROM account_file_erasures e WHERE e.user_id=account_deletions.user_id AND e.completed_at IS NULL)
          """);
        db.update("DELETE FROM account_file_erasures WHERE completed_at<CURRENT_TIMESTAMP-INTERVAL '1 day'");
    }
}
