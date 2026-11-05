# Governed appointment transitions

| Trigger | Previous | Result | Evidence |
|---|---|---|---|
| Explicit free direct booking | available slot | CONFIRMED | exact slot/policy/amount acceptance |
| Exceptional request | available slot | REQUESTED | explicit patient acceptance |
| Professional approval | REQUESTED | CONFIRMED or PAYMENT_PENDING | current scope; immutable price decides payment requirement |
| Paid direct booking | available slot | PAYMENT_PENDING | current provider and operating approvals |
| Exact provider capture | PAYMENT_PENDING | CONFIRMED | provider reference, amount, currency, current eligibility and unexpired reservation |
| Late or ineligible capture | pending/terminal booking | CANCELLED_BY_PLATFORM | charge retained; full refund obligation |
| Participant cancellation | REQUESTED, PAYMENT_PENDING, CONFIRMED | CANCELLED_BY_PATIENT/PROFESSIONAL | current revision and explicit reason |
| Unpaid request timeout | REQUESTED, PAYMENT_PENDING | CANCELLED_BY_PLATFORM | server deadline; slot remains cancelled |
| Reconciled professional no-show review | CONFIRMED | PROFESSIONAL_NO_SHOW | elapsed grace, operational review, no contradictory server readiness/service evidence |

SESSION_ACTIVE and COMPLETED require the P3 encounter path. The old generic status PATCH cannot operate on governed clinic appointments. A cancelled slot does not silently reopen. Rescheduling requires a new explicitly accepted offer and cancellation of the prior appointment; P5 provides the joined workflow. Retries return the original booking or conflict if a retry key changes meaning. Database constraints supplement schedule and patient locks. Full commercial behavior and production gates: [booking and financial operations](booking-and-financial-operations.md).
