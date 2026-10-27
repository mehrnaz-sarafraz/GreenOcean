# Controlled performance and reliability evidence

Measured executable4075561 in CI40541904020: real loopback HTTP → Spring/Java21 → disposable PostgreSQL18, test profile pool maximum3, at most4 concurrent synthetic workers. The72 timed requests below cover17 scenarios; measurements include request completion on that runner, not production network/client devices or staffed operation. p50/p95 use nearest-rank samples; for small n, p95 is simply the maximum. They are not statistically established SLOs.

| Scenario | Requests | p50 ms | p95 ms | max ms | HTTP outcomes |
|---|---:|---:|---:|---:|---|
| registration | 4 | 149.91 | 201.12 | 201.12 | 201×4 |
| verification | 4 | 51.11 | 51.56 | 51.56 | 200×4 |
| login | 4 | 155.72 | 239.66 | 239.66 | 200×4 |
| refresh | 4 | 22.29 | 26.18 | 26.18 | 200×4 |
| privacy-feed-page10 | 12 | 11.31 | 16.97 | 16.97 | 200×12 |
| profile | 12 | 7.44 | 11.38 | 11.38 | 200×12 |
| listener-ready | 1 | 9.66 | 9.66 | 9.66 | 200×1 |
| presence-heartbeat | 4 | 23.11 | 35.28 | 35.28 | 200×4 |
| last-slot-contention | 3 | 15.89 | 21.37 | 21.37 | 200×1, 409×2 |
| acceptance-contention | 2 | 9.15 | 11.01 | 11.01 | 200×2 |
| activation | 1 | 8 | 8 | 8 | 200×1 |
| message-send | 4 | 26.35 | 42.09 | 42.09 | 201×4 |
| message-poll | 4 | 14.07 | 18.14 | 18.14 | 200×4 |
| session-end | 1 | 6.56 | 6.56 | 6.56 | 200×1 |
| report-contention | 4 | 10.83 | 12.47 | 12.47 | 201×1, 409×3 |
| owner-export | 4 | 18.65 | 27.93 | 27.93 | 200×4 |
| account-erasure | 4 | 272.66 | 357.39 | 357.39 | 204×4 |

The initial K load run found duplicate report insertion (three201,one409). After transaction-scoped reporter/target serialization, the same unchanged real HTTP assertion gives one201 and three409; the additional real-PG4-thread test also verifies one stored open report. Last-slot contention produces one request and two conflicts; duplicate acceptance is idempotently200 without duplicate conversation. Four account erasures complete and the same-account journey confirms old bearer/login cannot recover the deleted account. No5xx occurred in these72 samples. Raw non-sensitive timings/statuses are in performance-results.json; no token, email or message body is recorded there.

Existing D resend/limit/outbox failure/cleanup tests, EF refresh/login/reset/revoke concurrency with pool3, I multi-instance transaction/expiry/rollback tests and J withdrawal race are re-executed in the final backend suite. Their wall times are test-suite evidence, not substituted as production request latency. OTP resend pressure and scheduled cleanup are functionally/race tested but not separately saturation-benchmarked here.

Limits: this is a bounded smoke load, not a capacity/stress/soak/large-history/multi-host test. Production hardware/region, long-lived locks/query timeouts, pool saturation, index/DDL contention at actual scale and real backup/RTO/RPO need staging qualification. No DB statement/lock timeout or real recovery objective is claimed verified merely because requests completed. Review query/lock/connection timeout settings and safe cancellation/rollback against the chosen production environment before pilot. Native network transitions remain device-required.

Proposed nonbinding staging discussion: declare hardware/pool/data shape and intended initial concurrency, require zero authorization/privacy/capacity violations/unexplained5xx, then discuss p95<2s reads/transitions and<5s password/erase. These values are NOT APPROVED customer commitments and are not hidden pass thresholds. Until an owner chooses rollout capacity and operations demonstrate results, no availability/throughput/RTO/RPO promise is made.
