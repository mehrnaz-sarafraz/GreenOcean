# Final integrated regression evidence

|Validated phase/source|CI run|Backend|Logic|Browser|Real stack|Four jobs|
|---|---|---:|---:|---:|---:|---|
|Q1 62c60dcdd08c4253d699083a68cd7413fde292da|41641548999|683|69|182|52|success|
|Q2 bfbdafbab9cca4755fc5cb6e4d9277a0be8bedea|41779968808|714|69|189|53|success|
|Q3 035879e196e99ed1ffc1097c3363acf7287040b0|41786139282|760|69|198|54|success|
|Final Q4 366adcc8140063540c13793305ca7cf4ba29a346|41964591756|797|69|205|54|success|
|Q5 64cff9785a02b2ab23a5061b3e28df9297435a20|41969366519|818|69|211|55|success|

Counts are scoped to each exact head; never add predecessor counts as new tests. Q4 initial374cee4/CI41890999180 counts are superseded by final Q4 above. Q5 local21 focused tests,176 affected cross-phase tests, six browser cases,69 logic cases and one real-stack journey also passed; frontend TypeScript passed and lint had0 errors/11 existing warnings. Web Persian/RTL at360/1280 is not native validation.

Q0 local patched-library run passed103 targeted cases with0 failures/errors/skips, including22 pilot workflow tests, identity/session/MFA, payments, clinical entry and restore privacy. Two new country/age regressions accompany the reproduced consent defect. The preserved red reproduction includes one actual failed assertion plus an explicitly documented fixture-consistency error; only the actual assertion is application-defect proof.

Final Q0 requires the existing four-job harness on its exact published head: complete Maven clean verify, populated upgrade/rollback and PostgreSQL contention, disposable synthetic restore/checksums; npm clean install, TypeScript/lint/logic, all-platform Expo export and all browser suites; real HTTP/browser/PostgreSQL/synthetic mail/WebAuthn journeys; fresh source patterns, published migration and dependency inventory. The exact final commit/run/counts are recorded in the final PR check evidence and delivery message; this report does not predeclare unexecuted CI success.

Q4's eight25/50/75/100-client runs, process kills, real PostgreSQL restart and isolated encrypted restore are retained with exact environment/source limitations. Final concurrency regressions recheck invariants with Q0 libraries, but Q4 latency numbers have not been remeasured as a Q0/production capacity promise. Hardware/device/provider/legal/staging-production recovery acceptance remains separate. The observed encrypted orphan from killed upload remains a documented reconciliation item, not silently deleted.
