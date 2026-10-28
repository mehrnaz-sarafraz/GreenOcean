# Phase N requirements matrix

| Requirement | Scope | Current evidence status |
|---|---|---|
| N-01 | Genuine listener scheduling | Verified N1: run 40694008890 |
| N-02 | Timezone-safe calendar architecture | Verified N1: run 40694008890 |
| N-03 | Actual bookable slots | Verified N1: run 40694008890 |
| N-04 | Reservation lifecycle | Verified N1: run 40694008890 |
| N-05 | Booking cancellation and rescheduling | Verified N1: run 40694008890 |
| N-06 | Appointment check-in | Verified N1: run 40694008890 |
| N-07 | Live-session and calendar capacity coordination | Verified N1: run 40694008890 |
| N-08 | Listener and member scheduling interfaces | Verified N1: run 40694008890 |
| N-09 | Queue entry eligibility | Verified N2: run 40702893879 |
| N-10 | Twenty-minute server-enforced queue expiry | Verified N2: run 40702893879 |
| N-11 | Fair queue assignment | Verified N2: run 40702893879 |
| N-12 | Queue-to-live handoff | Verified N2: run 40702893879 |
| N-13 | Queue cancellation and recovery | Verified N2: run 40702893879 |
| N-14 | Queue capacity and contention | Verified N2: run 40702893879 |
| N-15 | Queue notifications | Verified N2: run 40702893879 |
| N-16 | Preserve the direct Live Now pathway | Verified direct I in N1; preserved by N2 |
| N-17 | Thirty-minute planned support sessions | Implemented N3; final exact-head CI pending |
| N-18 | Limited consensual extension | Implemented N3; final exact-head CI pending |
| N-19 | No-show and delayed-start handling | Implemented N3; final exact-head CI pending |
| N-20 | Optional post-session feedback | Implemented N3; final exact-head CI pending |
| N-21 | Optional repeat booking | Implemented N3; final exact-head CI pending |
| N-22 | Private follow-up notes | Safely deferred per prompt: no durable note store; separate purpose remains unapproved |
| N-23 | Listener scheduling dashboard | Implemented N3; final exact-head CI pending |
| N-24 | Member Support dashboard | Implemented N3; final exact-head CI pending |
| N-25 | Regional and operational authorization | Implemented N3; final exact-head CI pending |
| N-26 | Privacy and data governance | Implemented N3; final exact-head CI pending |
| N-27 | Notifications and reminders | Implemented N3; final exact-head CI pending |
| N-28 | Safety, reporting and incident handling | Implemented N3; final exact-head CI pending |
| N-29 | Security and concurrency threat model | Implemented N3; final exact-head CI pending |
| N-30 | Final integration and release verification | Implemented N3; final exact-head CI pending |

N1 sources: ListenerCalendar, ListenerBookings, V52–V53, PhaseN1SchedulingTest, PhaseN1ConcurrencyTest, PhaseN1MigrationTest, frontend planning.tsx and phase-n1 browser journeys. Final run links belong in test-evidence.md.

N3 executable evidence: PhaseN3SessionsTest, PhaseN3ConcurrencyTest, PhaseN3RetentionTest, PhaseN3CalendarTest and PhaseN3OperationsTest; session-plan/history/operations UI and phase-n3 browser/real-stack journeys. Owner-selected retention: 30 days after completion. Physical-device, genuine staffing, legal/pilot, actual alert delivery and production restore/deletion replay remain external gates, not invented technical passes.
