# Capacity and concurrency
Phase I retains its existing short global transaction lock 72491001 for live admission and reconciliation. Calendar/reservation writes use ordered per-participant advisory locks, cross-role conflict checks, and GiST exclusion constraints. Capacity stays in PostgreSQL and survives process restart.

A booking protects [starts_at, reserved_until), including the central break and offer/activation headroom. A new live request checks both member and listener commitments at creation, acceptance and activation. The origin booking is excluded only for its own correctly bound scheduled handoff. Future bookings do not hold live readiness days in advance. An unbounded historical live session is treated conservatively as occupying capacity until it ends; existing sessions receive no retroactive timer.

Rescheduling locks all affected participants in UUID order and commits replacement and cancellation together. Distinct keys still respect the shared upcoming limit; the same key returns the original result. Exclusions are the final backstop. A transaction rejected by a database constraint rolls back before a generic 409 response.

N1 tests cover same-slot competition, duplicate keys/devices, concurrent upcoming-limit enforcement, cancellation versus reschedule, schedule removal versus reservation, repeated cancellation and pause races. Queue and extension coordination will be added by validated dependent N2 and N3 milestones.
