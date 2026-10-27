# Identity and permissions

Authentication, lifecycle status, age evidence, professional verification and operational authorization are independent. Public registration only assigns USER, even if clients send role/status/verification fields. PROFESSIONAL intent is rejected at registration; existing intent preferences remain advisory and never grant authority.

The HTTP interceptor covers all authenticated social/catalog/listener/professional routes by default, with explicit activation/owner/public exemptions. Active JWTs require an owned unrevoked session, a permitted live account status and a matching current role set. Administrative routes retain existing role checks and are additionally disabled by default; the production guard refuses administrative enablement until MFA is integrated.

Age evidence separates UNKNOWN/ADULT/INELIGIBLE from UNKNOWN/SELF_ATTESTED/INDEPENDENT. Public activation can only create self-attested evidence, preserves a previous independent-adult method and cannot override an independent ineligibility decision. There is no public route for issuing independent assurance. A future trusted provider must use authenticated callbacks, replay protection, provenance, minimal evidence, expiry and a separately reviewed server-side integration. Full birth dates are not collected, and supplied legacy birth year is not copied to new profiles.

Professional applications, document validation/storage, scan review and human approval are reused. Verified status is checked by the existing professional services. Care booking/messages additionally require explicit human-reviewed unexpired country scope for residence and any declared current country, plus actor/provider account eligibility and regional service availability. Renaming a profile or changing intended-role preferences does not change verification. Listener coursework and human approval are reused. Conversation service_kind prevents an already-created professional/listener conversation from bypassing the regional service gate through a generic message URL.

## Sessions by platform

Mobile retains SecureStore plus bearer access/rotating refresh tokens. Web access tokens exist in memory only; refresh tokens use a Secure, HttpOnly, SameSite=Strict cookie restricted to `/api/v1/auth/web`. Browser writes require an allowed Origin plus the `X-GreenOcean-CSRF: 1` header. CORS allows credentials only for configured origins; prod rejects HTTP and wildcard origins. Deploy web and API on the same site, or introduce a reviewed same-origin proxy/BFF before changing SameSite policy.

Old localStorage/sessionStorage token keys are purged, requiring browser users to sign in again. The shared refresh promise avoids normal concurrent reuse. Generation checks prevent late refresh results from restoring a signed-out session. All user-owned data providers remount on account identity changes. Multi-device and mobile secure-storage behavior still require device verification.
