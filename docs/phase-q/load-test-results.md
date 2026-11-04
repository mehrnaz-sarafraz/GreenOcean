# Q4 local load results — 2026-11-04

Evidence is synthetic local HTTP on Q3 base `035879e196e99ed1ffc1097c3363acf7287040b0` plus the Q4 worktree. Final immutable-head CI is recorded separately; these measurements are not production approval.

Hardware: AMD Ryzen5 4600H,6 physical/12 logical cores,7.42GiB RAM; Windows; JDK21, heap capped768MiB; PostgreSQL18.6, shared buffers128MiB, max connections100; application QA pool3. The client,server and sampler share one JVM and machine. Local storage was not independently benchmarked.

Each concurrency has two20-second measured windows with5-second warmup; one outstanding request per client and250ms think time. New free bookings and signed synthetic payment captures start during measurement; warmup uses reads for those paths. One clinical signaling participant,one purpose staff participant and10 booking/payment participants are included in every25–100-client cohort. All claimed credentials, notices, authentication stamps and provider receipts in this fixture are synthetic.

|Users|Run|HTTP requests|Requests/s|Successful p50/p95 ms|Protective responses|Unexpected failures|Sampled peak pool waiting|
|---|---|---|---|---|---|---|---|
|25|1|1770|87.3|19.323/107.955|74|0|7|
|25|2|1854|91.6|12.759/31.251|267|0|0|
|50|1|3585|176.9|15.801/101.729|1015|0|35|
|50|2|3739|184.8|10.433/29.283|1257|0|6|
|75|1|4935|242.9|24.437/230.356|1585|0|46|
|75|2|5325|262.9|19.428/84.302|1752|0|56|
|100|1|6906|340.7|19.981/149.119|2300|0|57|
|100|2|6939|342.5|15.403/174.784|2252|0|83|

All8 windows completed:35053 HTTP requests,0 unexpected responses. Protection responses are **rejections, not successful application work**. Shared loopback IP causes authentication/registration throttling; publishing duplicate/rate guards return fixed verified409 responses and429 is separately counted. The later windows therefore have a different success mix. The JSON artifact includes per-workload status counts and successful-only latency. No per-window/group reached10000 samples, so p99 is withheld. Do not pool these different windows to invent one reliable p99.

At final measurement the database had527 account records (including transient pending registrations from this and earlier exercises),2651 posts,2170 private entries and2129 mood observations. The active client cohort is100, with a separate100-account authentication cohort. This accumulated dataset exceeds the pilot registered-user target slightly; it does not enroll or authorize any real pilot participants. The resource inventory contains4 Growth Library records; visible course/resource representativeness still needs review before staging qualification.

Post-load invariant probes found161 governed appointments (80 new free bookings,80 paid bookings plus the synthetic current text episode),80 capture events,0 duplicate captures and0 duplicate slot bookings. These aggregate probes do not establish absence of unauthorized reads; current-purpose/participant/privacy denial assertions remain separate PostgreSQL and real-HTTP security tests. Refund/reward behavior is covered by the corresponding contention suites, not falsely represented as financial/reward throughput in this mix.

One-second resource observations found a peak83 clients waiting for a pool connection. No observed database deadlock increment occurred; inspect JSON for GC,CPU,heap,rollback deltas and per-window queues. Sampling can miss shorter peaks. Alert transport is not exercised by this mix; the bounded worker has its own signed HTTP timeout/retry/isolation tests.

Limits: short localhost series; shared-IP guard behavior; synthetic provider with no external latency; one clinical text episode and no audio/video load; incomplete representative content dataset; no staging infrastructure or real staffing. These results support a local technical observation, not a100-user production guarantee or launch decision. See pilot-capacity-recommendation.
