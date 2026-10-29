package com.greenocean.backend.professional.service;

import java.util.UUID;

/** Adapters must use durable idempotency keys and authenticate server-side provider evidence.
 * A browser return URL is never a receipt. No real provider has been selected/authorized. */
public interface ClinicPaymentProvider {
    String name();
    boolean authorized();
    record Intent(UUID id, long units, String currency, UUID appointment) {}
    record Evidence(String eventKey, String reference, String kind, long units, String currency) {}
    String create(Intent intent);
    Evidence reconcile(String reference);
    /** Reject missing/invalid signature, stale timestamp and wrong merchant before returning a reference. */
    default String verifyCallback(byte[] body, String signature) { throw new IllegalStateException("Authenticated provider callbacks are not configured"); }
    String refund(UUID refund, String captureReference, long units, String currency);
    default String subscription(UUID order, long units, String currency) { throw new IllegalStateException("Subscription billing is not configured for this provider"); }
    default String settle(UUID operation, UUID professional, long units, String currency) { throw new IllegalStateException("Authorized banking settlement is not configured for this provider"); }
}
