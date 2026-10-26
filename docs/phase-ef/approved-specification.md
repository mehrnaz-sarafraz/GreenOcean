# GreenOcean — Phase E + F
## Complete Authentication, Account Recovery, Session Security & Device Management Implementation

### ROLE

You are the principal application security architect, senior full-stack engineer, identity engineer, QA lead, and implementation owner for the existing GreenOcean project.

Your mission is to fully implement, integrate, harden, document, and test Phase E and Phase F as a single coordinated security workstream.

**Repository:** `mehrnaz-sarafraz/GreenOcean`

**Technology:**
- Java 21 / Spring Boot / Spring Security
- PostgreSQL / Flyway
- React Native / Expo / Expo Router / TypeScript
- Web, Android, and iOS

The result must be functioning source code, automated tests, documented security decisions, and a reviewable Pull Request.

**Do not stop at an audit, design, or recommendations. Execute the work.**

This is an existing project. Reuse correct implementations and preserve existing user accounts, roles, credentials, sessions where valid, social content, preferences, and previously approved contracts.

---

# 1. CRITICAL — Locate the correct implementation baseline

Phases A, B, C, and D have already been developed on stacked GitHub branches.

Previously observed:

- PR #1: `feature/phase-a-identity-privacy-access`
- PR #2: `feature/phase-b-welcome-trust-experience`
- PR #3: `feature/phase-c-registration-completion`
- PR #4: `feature/phase-d-email-verification-activation`

PR #4 was based on Phase C, not directly on main.

The last observed Phase D HEAD was:

`3edab57c89ee78aee29d5f6873e252ba45208de1`

These references may have changed.

### Mandatory first steps

1. Inspect current repository HEAD, branches, and PR status.
2. Identify the latest complete code baseline containing the most recent A–D changes.
3. Verify whether Phase D work has finished and whether its relevant CI checks pass.
4. Inspect only the necessary existing reports and test evidence.
5. Identify any additional active work that must not be overwritten.
6. Create a dedicated E/F feature branch from the correct baseline.

Suggested branch:

`feature/phase-ef-account-access-security`

Do not use stale main if that would omit unmerged Phase A–D code.

Do not merge earlier PRs automatically.

Do not repeat full A–D audits.

### Reuse classification

For every overlapping dependency, classify it as:

- VERIFIED_AND_REUSED
- REUSED_WITH_TARGETED_TESTS
- DEFECTIVE_REQUIRES_FIX
- INCOMPLETE_REQUIRES_EXTENSION
- BLOCKED_EXTERNAL_DEPENDENCY

A prior report is not sufficient evidence if the implementation or its dependencies have changed.

---

# 2. AUTHORITATIVE SECURITY REFERENCES

The project owner has approved these as the reference framework for E/F.

## Primary standard

NIST SP 800-63B-4 — Digital Identity Guidelines: Authentication and Authenticator Management

https://pages.nist.gov/800-63-4/sp800-63b.html

Use it for:

- Password rules
- Authentication assurance levels
- MFA
- Reauthentication
- Authentication and session lifetime
- Authenticator management
- Account recovery considerations

## Web/backend security verification

OWASP ASVS 5.0.0

https://asvs.owasp.org/

Use its actual published control IDs for authentication, session management, access control, security logging, and related requirements.

## Mobile security

OWASP MASVS

https://mas.owasp.org/MASVS/

Use MASVS-STORAGE, MASVS-AUTH, MASVS-NETWORK, MASVS-PLATFORM, and relevant privacy requirements.

## Implementation guidance

OWASP Authentication Cheat Sheet:

https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html

OWASP Session Management Cheat Sheet:

https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html

OWASP Forgot Password Cheat Sheet:

https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html

OWASP Multifactor Authentication Cheat Sheet:

https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html

OWASP Credential Stuffing Prevention Cheat Sheet:

https://cheatsheetseries.owasp.org/cheatsheets/Credential_Stuffing_Prevention_Cheat_Sheet.html

### Standards discipline

Before implementation:

- Verify the exact version of each reference.
- Read the relevant normative sections.
- Distinguish SHALL, SHOULD, and MAY.
- Distinguish standards requirements from GreenOcean-specific risk decisions.
- Record the actual ASVS/MASVS requirement identifiers.
- Never invent control IDs or imply a risk-based configuration is mandated by NIST.
- Do not claim full ASVS L2 compliance when password-only member authentication remains supported.
- Do not claim an AAL2 authentication event unless its necessary authentication factors and other applicable requirements are actually satisfied.
- Treat email verification, age eligibility, local device unlocking, and MFA as separate concepts.

---

# 3. REQUIRED SECURITY POLICY DOCUMENT

Create or update:

`docs/security/authentication-session-policy.md`

This must be the authoritative implementation contract.

For every policy decision document:

- Policy ID
- Threat or product risk
- Applicable standard and version
- Exact standard requirement or recommendation
- GreenOcean decision
- Reason for that decision
- Enforcement layer
- User-visible behavior
- Tests proving enforcement
- Limitations or exceptions
- Configuration and rollout implications

Do not merely copy standard text.

Create an additional machine-readable security control matrix if useful.

### Baseline assurance

Ordinary members:

- NIST AAL1-aligned password authentication.
- OWASP ASVS L1 as the minimum general application-security baseline.
- Additional higher-level ASVS controls applied wherever appropriate.

Sensitive access:

- Strong MFA with an AAL2-aligned target.
- MFA required before privileged administrative or moderation functionality is enabled.
- Phishing-resistant authentication must be offered when claiming an AAL2-aligned authentication service.
- WebAuthn/FIDO2 is the preferred privileged authentication method.

Do not automatically enable admin access when MFA is absent.

---

# 4. APPROVED GREENOCEAN SECURITY PARAMETERS

These are the accepted product policy values.

Implement them using server-authoritative, configurable policies.

| Parameter | Approved value |
|---|---|
| Single-factor password minimum | 15 Unicode code points |
| Supported password maximum | 128 Unicode code points |
| Access token lifetime | 15 minutes |
| Ordinary web session overall lifetime | 24 hours |
| Ordinary web inactivity timeout | 30 minutes |
| Ordinary mobile session overall lifetime | 30 days |
| Ordinary mobile inactivity timeout | 24 hours |
| Sensitive/privileged session overall lifetime | 12 hours |
| Sensitive/privileged inactivity timeout | 30 minutes |
| Local mobile sensitive-content background lock | 5 minutes |
| Password change | Revoke all sessions |
| Password reset | Revoke all sessions |
| Account suspension | Immediately deny existing access |
| Account deletion | Immediately deny/revoke access |
| MFA modification | Require strong recent reauthentication |

These session values are GreenOcean-specific decisions, not universal mandatory NIST numbers.

### Timeout design

Distinguish:

1. Access token expiry
2. Refresh token expiry
3. Overall authenticated-session timeout
4. Inactivity timeout
5. Recent-authentication/step-up freshness
6. Local sensitive-content lock on mobile

These are not interchangeable.

### Important rules

- Refresh token rotation MUST NOT reset the original overall session lifetime.
- Silent background polling MUST NOT incorrectly extend user inactivity.
- Overall and inactivity checks must be enforced by the server.
- Do not trust a client-side timer as the sole enforcement mechanism.
- Refresh-token expiry must never exceed the enforceable session boundary.
- Existing issued JWT access tokens must be denied after server-side session expiry or revocation.
- Distinguish authenticated activity from an application's automatic network traffic.
- Document the threat model for activity-signaling and never trust arbitrary user-supplied client timestamps as proof.
- Use trustworthy server timestamps and injectable test clocks when practical.
- Handle clock skew carefully.
- Separate limited PENDING activation sessions from ordinary ACTIVE social sessions.
- Do not turn a pending account into an active account just because it has a refresh token.
- Ensure cross-platform behavior is consistent with the approved policy.

### Mobile local lock

The five-minute mobile lock is a local privacy control, NOT server MFA or server reauthentication.

Use supported operating-system security mechanisms where appropriate.

Avoid storing a reusable plaintext unlock secret.

If secure local authentication is not supported, provide a safe fallback such as server reauthentication.

Never show an apparently working biometric lock that can be bypassed by trivial client-state manipulation.

Protect sensitive navigation state and previews while the application is locked.

Do not apply a five-minute hard logout unnecessarily to all public resources.

---

# 5. PHASE E — AUTHENTICATION AND ACCOUNT RECOVERY

Implement or verify all E-01 through E-15.

## E-01 — Email/password sign-in

Preserve the existing authentication service.

Verify:

- Consistent email normalization with Phase C
- Password authentication over protected transport
- Appropriate error handling
- Usable Web and native login screens
- Password manager compatibility
- Autofill and paste support
- Submit loading and duplicate-tap protection
- Recoverable timeouts and network failures
- Correct role/account-status transitions
- No unintended authentication persistence across users

Do not build a second login service.

## E-02 — Password hash verification

Reuse the existing compatible password encoder.

Previously observed:

- PBKDF2 for newly stored hashes
- BCrypt support for legacy accounts
- Opportunistic hash upgrade

Verify:

- Correct legacy authentication
- Upgrade without changing the password
- No plaintext persistence or logging
- Correct salt and work-factor handling
- Secure comparisons
- Appropriate error behavior for malformed stored hashes
- Safe migration from old hashes

Do not force a mass password reset without a justified reason.

## E-03 — Only appropriate accounts may authenticate

Distinguish:

- Successful credential verification
- Issuance of a limited activation session
- Issuance of a normal active session
- Authorization for a particular service

PENDING users may retain the existing tightly restricted activation flow.

Suspended and deleted users must not receive usable normal sessions.

An ACTIVE account still requires applicable age, role, MFA, and geographic authorization.

Never grant normal social permissions to a PENDING session.

Use the existing Phase A/D account lifecycle.

## E-04 — Safe authentication errors

Use generic external messages where necessary to prevent:

- Account enumeration
- Disclosure of suspension or account existence
- Timing-based information leaks
- Disclosure of password-hash state
- Excessive differentiation between unknown users and bad passwords

Support a safe verified account-owner flow for actionable account-status guidance.

Never show raw backend stack traces or security exception internals.

## E-05 — Login abuse prevention

Reuse Phase A/C rate-limiting infrastructure.

Protect against:

- Online password guessing
- Credential stuffing
- Password spraying
- Distributed attempts
- Mass enumeration
- Excessive password hashing work
- Repeated failed attempts from shared networks
- Abuse of login endpoints on both web and mobile

Use appropriate combination of account, source, endpoint, and broader security signals.

Do not rely exclusively on IP address.

Do not trust unvalidated X-Forwarded-For headers.

Use documented proxy trust boundaries.

Avoid introducing a trivially exploitable denial-of-service account lockout.

Provide meaningful Retry-After or recovery behavior where appropriate.

## E-06 — Forgot password

Reuse the Phase D mail delivery and Outbox infrastructure.

Provide a complete user journey:

LOGIN → FORGOT PASSWORD → REQUEST EMAIL → SECURE LINK → SET NEW PASSWORD → SESSIONS REVOKED → LOGIN

The recovery flow must work without an authenticated session.

Requirements:

- Generic response regardless of account existence
- Secure email-bound recovery
- HTTPS recovery links
- Authorized application deep linking where applicable
- Expiration
- One-time consumption
- Safe handling of leaked or malformed URLs
- No raw token leakage into analytics/logs
- Password manager and new-password autofill
- Clear success and failure states
- Correct accessibility and localization

Do not create another mail delivery service.

Do not claim email delivery is operational unless Phase D provider configuration has been verified.

## E-07 — Secure expiring recovery token

Reuse the existing high-entropy token system when correct.

Verify:

- Cryptographically secure generation
- Storage of only protected token verification material
- Strong token unpredictability
- Expiration enforced by the server
- Correct account binding
- No unauthorized token disclosure
- Proper invalidation when replaced or consumed

Evaluate token leakage through:

- Browser history
- URL logging
- Referrer headers
- Third-party scripts
- Browser analytics
- Deep links

Use appropriate no-store/no-referrer protection and safe token handling.

## E-08 — Single-use token

Ensure:

- Atomic consumption
- Only one concurrent use succeeds
- Replay fails
- Wrong-user binding fails
- Expired tokens fail
- Existing active sessions are not preserved after successful reset

Check persisted effects, not merely API status codes.

## E-09 — Session invalidation after password changes

The existing backend revokes all sessions after password change/reset.

Preserve this policy.

Complete frontend handling:

- Clear authentication state
- Clear sensitive credentials from component memory
- Invalidate cached private data
- Redirect to login
- Show truthful success feedback
- Prevent background token refresh from recreating an invalidated session

Test Web, Android, and iOS behavior where possible.

## E-10 — Authenticated password change

Review the existing Account & Security screen.

A previously observed bug:

- Backend requires at least 15 characters.
- An existing frontend AccountSecurity component still describes/enforces 12 characters.

Verify whether Phase D or other work corrected this.

If not, fix it.

Requirements:

- Current-password verification
- Consistent new-password policy
- Correct Unicode code-point counting
- Accessible visibility controls
- Clear field errors
- Loading and failure recovery
- No password leakage
- Safe duplicate-submission handling
- All-session revocation upon successful change
- Correct sign-out and reauthentication UX

Do not silently change the underlying approved password policy.

## E-11 — Password visibility and accessibility

Use a consistent shared approach for:

- Login
- Registration where applicable
- Password change
- Password reset
- Reauthentication

Requirements:

- Show/hide button with accessible label
- Password-manager compatibility
- Correct autofill attributes
- Paste support
- RTL/LTR correctness
- No accidental exposure in screenshots, logs, or analytics
- No cleartext persistence
- Appropriate behavior on focus changes

Reuse existing AppField functionality where suitable.

## E-12 — Passkeys for ordinary accounts

Priority P2.

Do NOT implement consumer-wide Passkey login in this phase.

Document an extensibility plan if useful.

However, privileged WebAuthn authentication required for E-14 is in scope.

Do not confuse privileged WebAuthn MFA with full consumer Passkey rollout.

## E-13 — Optional MFA for ordinary members

Priority P1, future rollout.

Document a clean extension contract.

Do not expose a nonfunctional MFA control to ordinary users.

Do not introduce a half-completed enrollment flow.

Preserve support for future optional phishing-resistant authentication.

## E-14 — Mandatory MFA for privileged access

Priority P0.

This requirement covers:

- ADMIN
- MODERATOR
- Other staff permissions with sensitive account/content access
- Professional or listener operations where the approved access-risk model determines MFA is mandatory

Do not blindly apply the same policy to every public professional profile action.

### Required behavior

A password alone must never unlock production privileged operations.

Use a real, independently verified MFA mechanism.

Preferred:

WebAuthn/FIDO2-based phishing-resistant authentication.

If the existing project already uses an equivalent suitable mechanism, reuse it.

### Implementation requirements

Inspect any existing MFA provider, feature gate, role middleware, account-security UI, and security configuration.

If no suitable implementation exists:

- Build a maintainable privileged MFA architecture.
- Implement supported enrollment and challenge verification where technically feasible.
- Bind authenticators to the correct user and relying party.
- Validate challenge, origin, RP ID, signature, and user verification requirements.
- Support secure authenticator registration/removal.
- Support recovery without bypassing the intended authentication strength.
- Prevent self-granting privileged roles.
- Prevent replay.
- Protect against login CSRF and session fixation.
- Record appropriate audit events.
- Apply rate limits.
- Gate privileged API actions server-side.
- Ensure both web and relevant mobile routes respect the requirement.

If full operational WebAuthn rollout is blocked by an unavailable origin, hardware, security key policy, or external dependency:

- Implement the secure configurable boundary and feasible tests.
- Keep privileged production access DISABLED.
- Report E-14 as BLOCKED_EXTERNAL_DEPENDENCY or IMPLEMENTED_NOT_FULLY_TESTED as appropriate.
- Never silently fall back to password-only privileged access.

Do not use email verification alone as an MFA replacement.

Do not claim NIST AAL2 merely because a second UI step appears.

### Privileged recovery

Define:

- Lost authenticator
- Stolen authenticator
- Compromised account
- Factor replacement
- Emergency administrative recovery

Recovery must not be weaker than the protected privilege level without an explicitly controlled, independently auditable exception process.

Avoid permanent administrative lockout without creating an insecure universal recovery bypass.

## E-15 — Security event logging and monitoring

Implement or complete a dedicated minimal security-event model.

Relevant events include:

- Successful login
- Failed login
- Rate-limited authentication
- Password reset requested
- Password reset completed
- Password changed
- Session revoked
- Logout all
- New authenticator enrolled
- MFA challenge failure/success where appropriate
- MFA authenticator removed
- Privileged access denied
- Suspicious refresh replay
- Account suspension affecting sessions

Requirements:

- No raw passwords
- No raw access or refresh tokens
- No recovery codes
- No sensitive wellbeing content
- No private messages or journal entries
- No unnecessary full IP retention
- Configurable retention
- Access controls for security logs
- Bounded cardinality and volumes
- Resistance to log injection
- Appropriate alert signals without noisy false alarms

Reuse existing audit/event infrastructure when suitable.

Do not create a parallel generic analytics product.

Never put mental-health browsing behavior into security logs.

---

# 6. PHASE F — SESSIONS, TOKENS AND DEVICES

Implement or verify all F-01 through F-16, plus F-17 through F-20.

## F-01 — JWT access tokens

Preserve existing JWT infrastructure.

Verify:

- Signature algorithm and key strength
- Issuer checks
- Expiration
- Relevant audience checks if adopted
- Unique token/session binding
- Rejected malformed claims
- No weak algorithm fallback
- No token acceptance after revoked session
- No stale privileged role acceptance

Do not replace working JWT code without a concrete reason.

## F-02 — Strong refresh token generation

Preserve cryptographic randomness and sufficient entropy.

Do not expose refresh tokens in URLs, logs, analytics, or public API responses not intended for native clients.

## F-03 — Protected refresh token storage

Verify database storage contains only token verification material, not raw bearer secrets.

Review unique indexes, retention and access to token records.

## F-04 — Refresh token rotation

Maintain single-use rotation.

Cover:

- Replay detection
- Family/session revocation
- Parallel refresh calls
- Multi-tab web traffic
- Retry after network interruption
- Crash recovery
- Token theft scenarios
- Consistent error behavior

Do not accidentally invalidate a legitimate session through ordinary frontend concurrency unless the security policy intentionally requires revocation.

Do not relax replay protection merely to hide race conditions.

## F-05 — Server-side session revocation

Verify revocation immediately invalidates relevant access and refresh tokens.

A revoked session must not remain authorized until JWT expiry.

## F-06 — Logout

Implement correct platform-specific logout semantics.

For web:

- Revoke the refresh session server-side.
- Clear the HttpOnly cookie through the correct server response.
- Clear memory-only access tokens.
- Clear sensitive client caches.

For native:

- Revoke server-side session.
- Clear SecureStore credentials.
- Clear cached private data.

### Offline logout

Do not claim server-side revocation succeeded when the network request failed.

Local logout should immediately hide private data and block local session reuse.

Provide secure retry/reconciliation semantics if server revocation is pending.

Never leave the browser silently able to restore a session from an uncleared cookie after the user believes they signed out.

Handle this limitation truthfully and safely.

## F-07 — Logout from all devices

Preserve the existing backend endpoint.

Add or complete an understandable user-facing control.

Requirements:

- Explicit confirmation
- Appropriate recent authentication
- Server-side revocation of all sessions
- Current-device behavior explained
- Safe retry/error states
- No stale cache or token restoration

## F-08 — Session inventory

Reuse the existing sessions endpoint and UI.

Show:

- Current session
- Safe device label
- Creation time
- Last meaningful use
- Expiration where useful
- Session-specific revoke action

Do not misrepresent user-supplied device labels as verified hardware identity.

Do not invent exact device geolocation.

Protect session metadata from other users.

## F-09 — Revoke individual session

Ensure:

- Ownership checks
- Cross-account denial
- Immediate server revocation
- Correct current-session behavior
- Loading, confirmation, and error feedback
- Retry safety
- No unauthorized database changes

## F-10 — Secure native credential storage

Preserve Expo SecureStore or suitable operating-system-backed storage.

Assess:

- Accessibility class/security level
- Android Keystore/iOS Keychain behavior
- Device migration and backup
- App uninstall/reinstall
- Platform-specific credential invalidation
- Error handling
- Sensitive data leakage to logs
- Rooted/jailbroken device limitations
- Backgrounding and app switching

Do not claim SecureStore guarantees immunity from compromised devices.

Use MASVS controls and relevant test methods.

## F-11 — Browser token security

Preserve the current Phase A memory-only web access token and HttpOnly refresh-cookie design, if correct.

Verify:

- HttpOnly
- Secure on production HTTPS
- Correct SameSite selection
- Narrow path/domain
- Correct cookie clearing
- No raw refresh token in JavaScript-readable storage
- No accidental token exposure in server-rendered HTML or URLs
- Correct CORS and credential handling
- Safe response cache headers
- Protection against XSS-assisted session abuse
- Compatible multi-tab operation

Do not move refresh tokens back into localStorage or sessionStorage.

## F-12 — CSRF protection

The previously observed implementation uses Origin validation and an X-GreenOcean-CSRF header for browser session endpoints.

Inspect its adequacy against all browser cookie-authenticated state-changing endpoints.

Requirements:

- Document the threat model.
- Verify exact trusted-origin configuration.
- Handle reverse proxies correctly.
- Reject forged cross-origin writes.
- Avoid wildcard credentialed origins.
- Use anti-CSRF tokens or another standards-aligned defense where required.
- Do not assume SameSite alone is sufficient.
- Do not assume one custom header protects endpoints that do not check it.
- Test cross-origin requests and same-site hostile subdomains where applicable.

Do not globally disable meaningful CSRF protection merely because JWT endpoints are stateless.

## F-13 — Concurrent refresh

Reuse existing frontend single-flight and backend transactional protections.

Test:

- Several requests receiving 401 together
- Two browser tabs refreshing
- Multiple native requests
- Slow responses arriving out of order
- Network interruption during rotation
- Sign-out during refresh
- Account switching during refresh

The final session state must be safe and deterministic.

## F-14 — Cross-account data isolation

This is critical for a mental-health community.

When user A signs out and user B signs in on the same device:

- No private feed from A may be displayed to B.
- No private messages from A may be displayed.
- No journal or wellbeing data from A may appear.
- No cached professional/consultation data may cross accounts.
- No outgoing request from A may update B's state.
- No stale React state may briefly reveal sensitive A content.

Use generation/epoch or cancellation mechanisms and user-scoped cache keys where necessary.

Do not rely only on hiding the account name.

## F-15 — Session expiry UX

When a session expires:

- Stop protected API access.
- Clear sensitive client state.
- Show understandable sign-in guidance.
- Avoid redirect loops.
- Preserve only safe, non-sensitive navigation intent where permitted.
- Never use an expired refresh token to reestablish a forbidden session.
- Do not expose private data while auth state is loading.

Differentiate network errors from actual authentication expiration.

## F-16 — Role and session authorization

Preserve existing live role and session validation.

Test:

- Role removal
- Role elevation without MFA
- Account suspension
- Account deletion
- Pending-account limited access
- Professional verification revocation
- Admin disabled state
- Access with stale JWT claims

Backend must remain the authoritative source of privilege.

## F-17 — Overall and inactivity limits

Implement the approved timeouts from Section 4.

Potential data fields, subject to actual schema:

- originalAuthenticatedAt
- lastMeaningfulActivityAt
- overallExpiresAt
- idleExpiresAt
- assuranceLevel or session assurance metadata
- privilegedAuthenticationAt

Avoid duplicating equivalent fields.

### Enforcement

- Enforce timeouts at the server.
- Deny access even when the JWT signature and exp claim remain otherwise valid.
- Bind refresh rotation to the original session lifetime.
- Keep timeout semantics clear for pending, ordinary and privileged sessions.
- Make legitimate activity updates efficient and concurrency-safe.
- Ensure background traffic does not silently extend inactivity.
- Do not accept arbitrary client-provided timestamps.
- Avoid unnecessary per-request write contention and database overload.

Use additive Flyway migrations where required.

### Migration

Protect existing user sessions.

Define how old sessions without new timeout fields are assigned safe bounded lifetimes.

Do not silently give them an unlimited lifetime.

Avoid mass logout unless required and communicated by a reviewed rollout policy.

## F-18 — Step-up authentication

Create or reuse a secure recent-authentication mechanism for sensitive operations:

- Password change
- Email/security-contact change
- MFA enrollment/removal
- Logout all
- Account deletion
- Other high-impact actions identified in the threat model

A recent access token alone must not serve as proof of recent reauthentication.

Use server-verifiable authentication freshness.

For password-only member accounts, require appropriate password reauthentication.

For privileged accounts, use an approved stronger method.

For a GreenOcean-specific default, use a configurable recent-authentication window of 10 minutes unless the existing approved implementation establishes an equivalent stricter rule.

Document this 10-minute value as a risk decision, not an explicit universal NIST requirement.

Do not allow account recovery to bypass privileged MFA automatically.

## F-19 — Suspicious access detection and notification

Implement or complete minimal high-value security signals:

- New session established
- Password changed
- Account recovery completed
- New privileged authenticator registered
- Existing privileged authenticator removed
- Refresh token reuse suggesting compromise
- Logout all
- Privileged access from an unexpected context

Use privacy-preserving rules and avoid excessively precise tracking.

Provide user notifications through existing verified channels where appropriate.

Do not claim an email was delivered unless the mail system confirms its actual status.

Do not disclose private mental-health activity in notification content.

Add operational alert thresholds and monitoring where possible.

## F-20 — Sensitive cache and local data cleanup

Audit:

- React state
- Data providers
- Query/cache libraries
- SecureStore
- Browser memory
- Browser storage
- Background requests
- Local drafts
- Mobile navigation state
- Screenshots/app switcher preview
- Local files and exports

Implement safe cleanup and per-user isolation when:

- Signing out
- Switching accounts
- Deleting an account
- Losing session validity
- Revoking a device
- Resetting or changing a password
- Returning from a locked mobile background state

For unsent sensitive user drafts, define a privacy-preserving approach that avoids both unauthorized disclosure and unnecessary loss of content.

Do not silently persist mental-health text in unencrypted storage to avoid draft loss.

---

# 7. FRONTEND EXPERIENCE

Reuse existing navigation:

Profile → Account & Security

Do not create a competing security settings module.

The final user-facing experience should include:

### Password section

- Change password
- Accessible show/hide controls
- Consistent password policy
- Confirmation where existing design requires it
- Correct sign-out after success

### Active sessions

- Current device
- Other active sessions
- Revoke individual session
- Logout from all devices
- Honest device metadata
- Loading and error feedback

### Account recovery

- Forgot password from login
- Secure recovery link
- Valid/expired token screens
- Choose new password
- Return to login

### MFA section

- Functional privileged enrollment/verification when available
- Secure factor management
- Clear operational status
- No fake enabled status
- No decorative setup buttons that do nothing

### Security activity

- Appropriate recent security events
- Useful event labels
- No unnecessary tracking information

### Accessibility

- Screen reader
- Keyboard navigation
- Focus management
- Error announcements
- Touchable targets
- Text enlargement
- EN/FA and existing supported localization
- RTL/LTR password/email handling
- Password manager autofill

Use existing design tokens and shared components.

---

# 8. NON-DESTRUCTIVE DATABASE AND COMPATIBILITY RULES

Inspect existing Flyway migrations before changing schema.

Rules:

- Never rewrite applied migrations.
- Add new numbered migrations only.
- Preserve valid credentials.
- Preserve existing user roles.
- Preserve existing active-session data where safely compatible.
- Preserve password hash upgrade compatibility.
- Preserve deletion and account-assistance workflows.
- Preserve email Outbox architecture.
- Preserve legal policy acceptance evidence.
- Do not reset a database to make a test pass.
- Test migration on fresh and populated synthetic legacy databases.
- Document backfill and expiration strategy for historical sessions.
- Use safe rollouts or feature gates where necessary.
- Do not use production credentials or real personal information in tests.

Prefer forward-fix migration strategies when rollback is unsafe.

---

# 9. MANDATORY FOUR-PERSONA TEST MODEL

All relevant E/F requirements must have real test coverage.

## Persona A — Happy Path

Test:

1. Registered eligible adult signs in.
2. Password is verified correctly.
3. Access token is issued.
4. Refresh token is issued securely.
5. Protected resources are accessible.
6. Token refresh succeeds.
7. Session inventory shows the current session.
8. Another device signs in.
9. One device is revoked.
10. The revoked device immediately loses access.
11. Password change succeeds.
12. All previous sessions become invalid.
13. User signs in with the new password.
14. Password recovery works using controlled email delivery.
15. Privileged user completes approved MFA before privileged operations.

## Persona B — Creative User

Test:

- Long Unicode password
- Persian/RTL UI
- Password manager autofill
- Browser multi-tab login and refresh
- Android/iOS background and foreground
- Switching devices
- Switching between accounts
- Network delay during refresh
- Interrupted password reset
- User with multiple valid sessions
- Simultaneous requests close to timeout boundary
- Legitimate user on a shared network
- Safe handling of Unicode device labels
- Local lock/unlock experience
- Protected session expiry with local non-sensitive navigation retained

## Persona C — Confused User

Test:

- Incorrect password
- Unknown email
- Mistyped current password
- Expired recovery token
- Reused recovery link
- Password policy mismatch
- Lost internet
- Logout while offline
- Reopening app after session expiry
- Forgotten MFA authenticator
- Multiple taps on Logout All
- Device revocation error
- Sign-out during refresh
- Browser reload during authentication
- App restart during step-up
- Session expires while composing a draft

Provide understandable, recoverable UI states without leaking secrets.

## Persona D — Malicious User

Test:

- Credential stuffing simulation
- Password spraying
- Account enumeration
- Refresh token replay
- Forged JWT
- Invalid signature
- Expired JWT
- Old role claim
- Session ID substitution
- Cross-account session revocation
- CSRF on web auth endpoints
- Forged Origin
- Hostile subdomain assumptions
- Stolen refresh token
- Concurrent token rotation
- Unauthorized MFA enrollment
- MFA bypass attempts
- Step-up bypass with old login
- Password recovery token reuse
- Old session after password change
- Account suspension with existing token
- Cache exposure across accounts
- Pending user attempting protected social access
- Privileged user attempting sensitive action without MFA
- Injected unexpected JSON fields
- Malformed device names and session identifiers

All adversarial tests must run in isolated environments with synthetic test accounts.

---

# 10. REQUIRED AUTOMATED TEST LAYERS

## Unit tests

Cover:

- Password policy
- Password hash compatibility
- Authentication status decisions
- Session lifetime calculations
- Inactivity calculations
- Recent-authentication freshness
- MFA factor validation
- Rate limits
- Token validation
- Secure event classifications

## Backend integration tests

Use an isolated real PostgreSQL database.

Cover:

- Login
- Password failure
- Reset
- Change password
- Session creation
- Refresh rotation
- Replay
- Logout
- Logout all
- Session revocation
- Expiration
- Account suspension
- Account deletion
- Account pending restrictions
- MFA and step-up
- Cross-account session ownership
- Legacy session migration

Verify HTTP responses AND persisted database effects.

## Concurrency tests

Use independent database transactions and controlled synchronization.

Cover:

- Two simultaneous refresh attempts
- Logout racing with refresh
- Password reset racing with refresh
- Session revocation racing with protected API access
- Two parallel MFA verifications
- Session expiry during token issuance

## Frontend component tests

Cover:

- Login validation
- Password visibility
- Forgot password
- Recovery links
- Change password
- Active sessions
- Revoke session
- Logout all
- Expired session UX
- Account switching
- Local lock states
- MFA UI
- RTL and accessibility

## Browser E2E

Use actual GreenOcean frontend components.

Where possible, run full-stack browser tests against:

- Running Spring Boot backend
- Disposable PostgreSQL
- Controlled email capture service
- Controlled authentication/MFA test fixtures

Test cookie handling and real session behavior.

Clearly distinguish mock-transport tests from true full-stack E2E tests.

## Native validation

Run existing Android/iOS automated tests when practical.

Otherwise provide explicit manual test plans.

Do not mark native behavior PASS if only a JavaScript export completed.

## Standards evidence

Create an E/F control coverage matrix:

- Standard/version
- Verified control identifier
- Requirement
- Implementation location
- Test reference
- Result
- Remaining gap

Do not claim certification.

---

# 11. THREAT MODEL AND PRIVACY REVIEW

Create or extend:

`docs/security/phase-ef-threat-model.md`

Include:

- Credential theft
- Credential stuffing
- Password reset takeover
- Session fixation
- Session hijacking
- Refresh token theft and replay
- XSS-assisted session misuse
- CSRF
- Browser cookie scope mistakes
- Reverse-proxy trust errors
- Privileged MFA downgrade
- Lost authenticator recovery abuse
- Insecure device metadata
- Cross-account data leaks
- Local mobile storage compromise
- Stale background responses
- Sensitive data in logs or telemetry
- Brute-force resource exhaustion
- Account lockout denial-of-service

For each threat:

- Describe likely impact in GreenOcean.
- Identify existing mitigation.
- Implement missing mitigation where within scope.
- Add a test where applicable.
- Record residual risk.

Do not collect mental-health information for fraud detection without an independently justified need.

---

# 12. IMPLEMENTATION PACKAGES

Implement this as one coordinated workstream using focused, reviewable commits.

## EF-00 — Baseline and standards mapping

- Find latest A–D code.
- Build gap matrix.
- Verify standards.
- Document approved security policies.
- Establish exact file-level plan.

## EF-01 — Login and credential recovery

Implement missing E-01 through E-11 behavior.

Fix inconsistent 12/15-character copy and validation if still present.

Preserve existing password storage.

## EF-02 — Token and session lifecycle

Implement missing F-01 through F-17 behavior.

Add server-enforced absolute and idle limits.

Preserve rotation, replay protection, and existing session compatibility.

## EF-03 — Account Security frontend

Complete:

- Password change
- Sessions
- Logout all
- Recovery UX
- Security event presentation
- Cross-account isolation
- Mobile local lock where technically supported

## EF-04 — Privileged MFA and step-up

Implement E-14 and F-18.

Ensure production privileged access fails closed if MFA dependencies remain unavailable.

Document E-12/E-13 as future scope.

## EF-05 — Security event monitoring

Implement E-15 and F-19.

Reuse existing audit/mail infrastructure where possible.

## EF-06 — Sensitive data cleanup

Implement F-20 and relevant logout/session-expiry improvements.

## EF-07 — Testing, regression and delivery

Run four-persona testing, relevant existing regressions, migration tests, CI and final review.

You may reorder tightly coupled tasks to avoid unsafe intermediate behavior.

Do not leave a newly introduced security policy partially enforced between backend and frontend deployments.

---

# 13. MANDATORY RELEASE AND ROLLOUT SAFETY

Before considering release:

1. Determine whether session-policy changes invalidate old cookies or mobile tokens.
2. Create a documented compatibility/backfill plan.
3. Test old users with PBKDF2 and BCrypt hashes.
4. Test old mobile sessions.
5. Test browser refresh and cookie clearing.
6. Test user account changes.
7. Review MFA deployment prerequisites.
8. Verify privileged routes cannot bypass required MFA.
9. Verify production proxy/origin configuration.
10. Verify mail recovery dependency from Phase D.
11. Verify monitoring works without sensitive data exposure.
12. Document what remains blocked and must stay disabled.

No production data migration or automatic deployment without explicit authorization.

Do not merge into main automatically.

---

# 14. FINAL DELIVERY REPORT

Create:

`docs/phase-ef/delivery-report.md`

Include the following.

## A. Complete requirements matrix

List E-01 through E-15 and F-01 through F-20 individually.

For each:

- Initial status
- Previous implementation reused
- Actual defect or gap
- Change made
- Changed files
- Relevant standard
- Test IDs
- Actual test results
- Final status

Allowed final statuses:

- COMPLETE_AND_TESTED
- IMPLEMENTED_NOT_FULLY_TESTED
- BLOCKED_EXTERNAL_DEPENDENCY
- DEFERRED_APPROVED_SCOPE
- NOT_IMPLEMENTED

## B. Policy documentation

Include the approved security values, standards references, and any justified deviations.

## C. Code changes

Explain:

- Backend
- Frontend
- Database
- Browser security
- Native storage
- MFA
- Logging/monitoring

## D. Test evidence

Provide:

- Exact commands
- Unit tests passed/failed/skipped
- Integration tests passed/failed/skipped
- Concurrency results
- Browser E2E results
- Mobile test results
- Migration results
- CI checks
- Unexecuted tests

Never fabricate test success.

## E. Four-persona matrix

For each persona, map scenarios to requirement IDs and real test results.

## F. Security verification

State which security properties were demonstrated and by what evidence.

Do not claim absolute security or certification.

## G. Remaining dependencies

Report:

- Email provider configuration
- MFA origin/credential operational requirements
- Approved privileged-user rollout
- Missing native-device tests
- Missing monitoring infrastructure
- Other material release blockers

## H. Pull Request

Provide:

- Feature branch
- PR link
- Commits
- Migration list
- CI status
- Reviewer checklist
- Rollout notes
- Residual risks

---

# 15. DEFINITION OF DONE

The E/F workstream is complete only when:

1. All E and F requirements have been assessed against the actual latest code.
2. Completed A–D features have been preserved.
3. Password rules are consistent across all screens and the backend.
4. Login and account recovery function correctly.
5. Reset/change password invalidates the required sessions.
6. Access and refresh tokens follow the approved lifecycle.
7. Absolute and inactivity session limits are server-enforced.
8. Refresh rotation cannot extend an authenticated session beyond its absolute lifetime.
9. Browser cookies and CSRF defenses are correctly validated.
10. Native credentials are stored securely.
11. Logout and remote session revocation work.
12. Cross-account private-data isolation is verified.
13. Privileged endpoints cannot be used without required MFA.
14. Security activity is logged without leaking sensitive content.
15. Sensitive operations require appropriate recent authentication.
16. Account Security UI is accessible and accurate.
17. Happy, Creative, Confused, and Malicious test classes are implemented.
18. Actual test execution evidence is available.
19. Legacy accounts and compatible database history are preserved.
20. A reviewable Pull Request and security-policy documentation exist.

A feature blocked by unavailable operational dependencies must NOT be marked COMPLETE_AND_TESTED.

---

# FINAL EXECUTION DIRECTIVE

Begin with the latest actual Phase D implementation baseline.

Verify existing work rather than duplicating it.

Read the relevant NIST, OWASP ASVS, MASVS, and Cheat Sheet requirements directly.

Create the approved security-policy document and control matrix.

Then implement all missing, incomplete, defective, or insufficiently tested E/F requirements.

Preserve A–D security boundaries.

Use one consistent session and authentication architecture across the backend, web, Android, and iOS.

Do not weaken existing protections to make tests pass.

Do not substitute mock authentication for actual privileged MFA.

Do not claim an operationally blocked feature has been completed.

Do not stop after analysis or planning.

Proceed through code implementation, migration, automated testing, targeted regressions, documentation, focused commits, and a reviewable Pull Request.

**The final outcome must be a standards-grounded, privacy-preserving, comprehensively tested authentication and session-management system suitable for GreenOcean's sensitive peer-support environment.**
