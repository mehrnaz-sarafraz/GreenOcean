# Proposal state and review integrity

Owner: create DRAFT; edit DRAFT/NEEDS_REVISION with exact revision and increment it; submit to SUBMITTED; cancel DRAFT/NEEDS_REVISION/SUBMITTED to CANCELLED. Terminal APPROVED/REJECTED/CANCELLED cannot be silently edited or reapproved.

Independent current platform reviewer: SUBMITTED exact revision to APPROVED, NEEDS_REVISION or REJECTED with bounded explanation. Application role checks complement interceptor MFA/session/region gates. Approval locks the proposal row; group+owner+rules+decision+audit+notice commit together. Concurrent approvals have one winner; no duplicate active group. Changed or ineligible owner fails activation.

User responses omit reviewer identity. Queue reads are bounded and audited; proposals never appear in group directory/search. Local pattern screening is limited, while actual human criterion review remains required. Default-off intake and activation are separate gates; pending has no fabricated staff outcome or response deadline.
