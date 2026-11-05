package com.greenocean.backend.professional.service;
import java.util.UUID;import java.time.Instant;
/** No real vendor is approved. Implementations must enforce audience, session binding,
 * short expiry, single-use admission, revocation, region/residency and no recording/transcription. */
public interface ClinicalMediaProvider {
 boolean authorized(String country,String mode);
 record Admission(String endpoint,String token,Instant expiresAt,boolean recording,boolean transcription){}
 Admission admit(UUID encounter,UUID participant,UUID authSession,UUID oneTimeAdmission,String mode,Instant expiresAt);
 void terminate(UUID encounter);
 void revoke(UUID encounter,UUID participant,UUID authSession);
 boolean connected(UUID encounter,UUID participant,UUID authSession);
}
