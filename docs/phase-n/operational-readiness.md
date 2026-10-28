# Operational readiness
All scheduling, booking, queue, duration and follow-up feature gates default false. Underlying Phase I live support must also be authorized and enabled. Production staffing, region/country review, privacy purpose/retention approval, legal copy approval, actual security-key registration, alert destinations and native-device validation are external release gates. CI synthetic accounts do not satisfy them.

Disabling booking cancels pending reservations and their pending Phase I handoffs. Existing active conversations retain their captured timer and all Phase I safety controls; disabling underlying live support terminates open live sessions. Pausing a listener calendar stops future discovery without silently cancelling existing bookings.

Persisted expiry reconciliation runs with Phase I reads and its scheduled sweep. Repeated sweeps are idempotent. PostgreSQL is authoritative after restart. No production clock override, real push delivery, or production enablement is included.
