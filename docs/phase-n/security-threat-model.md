# Security and concurrency verification
Authority is the current JWT/session/region/role state and server-owned participant IDs. Booking, queue and session IDs are not bearer permissions. DTOs accept choices and retry keys, not caller-selected actors, queue position, expiry, no-show results or consent for another participant. Queries are owner-scoped; independent staffing needs current role plus recent security-key authentication.

PostgreSQL exclusion/unique constraints, atomic transition guards and I's advisory lock coordinate acquisition. Pair locks protect interval acquisition/extension; reconciliation releases capacity without accumulating unnecessary participant locks. Serialization/deadlock/constraint losers roll back and return a generic recoverable 409; a worker retries persisted state on its next pass. No process-memory queue or client timestamp authorizes activity.

| Required race | Executable evidence |
|---|---|
| Same final booking slot | N1 sameSlotHasOneCommittedWinner |
| Same listener matching | N2 twoMembersAndWorkersCannotShareOneListener |
| Immediate vs queue | N2 immediateRequestAndQueueAssignmentHaveOneCapacityWinner |
| Check-in vs queue | N2 scheduledCheckinAndQueueAssignmentUseDifferentEligibleCapacity |
| Two workers | N2 twoWorkersOfferExactlyOnce |
| Cancel vs proposal | N2 queueCancellationAndProposalReleaseAllPendingCapacity |
| Expiry vs readiness | N2 expiryAndReadinessRaceCannotCreateLateRequest |
| Check-in vs cancel | N2 checkinAndCancellationHaveOneTerminalOutcome |
| Qualification vs scheduled handoff | N2 scheduledHandoffAndRevocationCannotLeaveLiveAccess |
| Cancel vs reschedule | N1 cancelAndRescheduleHaveOneConsistentOutcome |
| Two-device queue | N2 sameMemberQueueingOnTwoDevicesRetainsOneDeadline |
| Two-device booking | N1 twoDevicesWithSameKeyGetSameReservation |
| Concurrent participant approvals | N3 concurrentParticipantApprovalsAddOnlyTenMinutes |
| Duplicate extension | N3 duplicateApprovedRequestsNeverAddTwentyMinutes |
| Extension vs next booking | N3 extensionVersusNextReservationHasOneCapacityWinner |
| End vs extension | N3 endingVersusApprovalNeverReopensSession |
| Reconnect vs duration | N3 reconnectAtDurationBoundaryCannotRestartClock |
| No-show vs late check-in | N2 noShowAndLateCheckinCannotReviveReservation |
| Schedule removal vs reservation | N1 deletingAvailabilityAndReservingCannotOrphanBooking |
| Restarted allocation | N2 restartedWorkerRetainsAllocationAndIndependentSingleTransfer |

Additional tests reject forged deadline renewal, transfer without a bound I request, direct timer changes without mutual consent, proposal theft, stale MFA/roles, cross-account feedback/history and clinical-text collection. UI generations, admission ordinals and revisions reject stale protected responses after navigation/backgrounding. Rate limits and bounded payloads constrain replay/flooding. All old I acceptance, activation, interruption, reconnect, block and message assertions remain in the regression suite.
