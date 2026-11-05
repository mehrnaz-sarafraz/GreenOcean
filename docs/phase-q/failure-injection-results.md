# Q4 controlled failure evidence

All data,accounts,credentials,receiver endpoints and providers below are synthetic. Nothing was deployed or activated for real users.

|Failure|Actual exercise|Result / limit|
|---|---|---|
|PostgreSQL restart / transport loss|Stop/start owned local PostgreSQL while the application retains its connection pool and a valid authenticated private-read request|Initial implementation returned misleading401. Session verification now distinguishes transient database failure: actual repeat returned200→503→200,Retry-After5,acknowledged private entry preserved; interruption-to-read3.170s. This is one local probe,not every service RTO.|
|Backend dies during booking|Owned child JVM killed at an observed PostgreSQL BEFORE INSERT sleep barrier; restart; repeat exact client key twice|No committed booking before crash; exactly one after retry; established endpoint200 contract preserved. Restart/retry16.266s.|
|Backend dies during queue admission|Same physical process kill at queue INSERT; restart; synthetic listener explicitly becomes available again through its real API; repeat client key|No abandoned queue row; exactly one after retry;201. No reboot-created human presence. Restart/retry16.219s.|
|Backend dies during sensitive upload|Kill after authenticated ciphertext is written but before metadata INSERT commits; restart and retry upload|No committed document before crash; exactly one quarantined metadata record after retry. One extra encrypted unreferenced object is identified and preserved for reconciliation; no CLEAN state is fabricated. Restart/retry16.032s.|
|Slow query / cancellation|Real PostgreSQL pg_sleep with transaction-local100ms statement timeout; rollback and reuse pooled connection|Bounded failure; subsequent query succeeds.|
|Notification/alert receiver outage,timeout and duplicate workers|Q3 actual signed HTTP receiver tests: receipt versus human ack,retry cap5,deduplication,escalation,concurrent claim,expired-claim recovery and slow-receiver executor isolation|Existing implementation reused; final targeted regression recorded in test-evidence. Process-kill drills kill the whole backend,not a separately claimed real delivery worker. No real recipient delivery/response proof.|
|Payment timeout / duplicate callbacks|P2 synthetic-provider state/timeout/reconciliation tests and80 exact synthetic captures under mixed HTTP load|No fabricated real payment;0 duplicate capture events. No real provider selected or qualified.|
|Media interruption|P3 admission/reconnect/unapproved-provider regressions|Single-use participant/session binding; timeout remains technical failure. No real audio/video connection was performed.|
|Partial object/key loss|Q4 real-filesystem ciphertext missing/tampered/wrong application binding/missing historical key tests; restore same authenticated bytes and verify original plaintext|Private storage boundary fails closed. These tests do not prove a production object-store inventory or key recovery.|

The child crash script accepts only an explicitly opted-in uniquely named loopback database,starts its own test JVMs and kills only those handles. Delay triggers exist only in that disposable database and are removed after rollback. The test server and fixture provisioning are test classes,excluded from the production jar. Fixture tokens stay in ignored target files. The source database is neither deleted nor overwritten.

Remaining operational gates: independent network partition/staging failover,real provider/recipient outages and human response,production object inventory/key custody and approved recovery rehearsal. The identified encrypted upload orphan is an expected reconciliation item,not evidence that cleanup is finished. No irreversible cleanup is automated by this change.
