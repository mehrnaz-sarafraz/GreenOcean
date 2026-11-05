# Service-specific operational quality

`GET /api/v1/operations/quality?domain=…` requires the default-off operations purpose, current explicit service role and strong recent authentication. Its transaction retains current-actor authorization while reading aggregates. It never reads journal bodies, support transcripts, clinical notes, private feedback narratives or unrestricted group content. The English/Persian operations screen shows purpose-specific counts with an explicit warning: reliability is not a mental-health or therapy outcome score.

|Service|Current measurement source|Interpretation / limitation|
|---|---|---|
|Social/groups|Imported source reports: status and oldest age; purpose-specific trust/incident queues|Unimported source reports are not included. No conclusion about misconduct is inferred from report volume|
|Private wellbeing/accounts|Fixed aggregate request buckets for wellbeing reads/writes, account archive and erasure requests; HTTP status families and elapsed totals|No text inspection or inferred distress. An accepted erase request is not proof of removal from files, providers or backups|
|Listener support|Existing queue/session state counts; qualified coverage and eligibility remain enforced by I/N source services|Calendar hours or a route acknowledgement are not evidence of continuous actual presence|
|Clinical services|Booking and encounter state counts, technical HTTP families, existing P4 voluntary source-feedback sample band only for a current reputation operator|No patient identities, encounter narratives or fabricated therapy effectiveness. Sample below five stays insufficient evidence|
|Financial operations|Payment-intent and refund states from P2|No fake settled funds, automatic obligation deletion or inference that a transport callback is financial confirmation|
|Education|Published/retired resources and technical request aggregates|Lesson completion and rewards are not clinical improvement or professional competence|
|Platform|Purpose-specific incident backlog and fixed aggregate telemetry|Process-local counters reset on restart; unavailable telemetry is not a zero-failure claim|

Valid feedback remains in existing P4: an eligible completed appointment/encounter, voluntary choices, one current feedback object per source, immutable versions, independent moderation/privacy review, correction/withdrawal and a separate complaint process. Repeated sessions from the same participant cannot fabricate independent sample size. A negative experience does not remove access to care or free booking. No new sensitive questionnaire or duplicate scoring service is created.

Q3 adds three fixed telemetry categories to Q2's ten: WELLBEING_WRITE, ACCOUNT_EXPORT and ACCOUNT_ERASURE. There are thirteen fixed service/request categories × five status families, never user/object identifiers or arbitrary labels. FINANCE does not inherit all CLINIC technical data. Counts and elapsed totals do not supply statistically valid p95/p99; Q4 measures controlled workloads independently.
