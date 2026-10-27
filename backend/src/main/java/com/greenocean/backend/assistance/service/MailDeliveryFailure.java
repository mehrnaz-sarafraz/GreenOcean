package com.greenocean.backend.assistance.service;
/** Safe classifications only: never retain provider body, recipient or delivery secret in an exception. */
public class MailDeliveryFailure extends RuntimeException {
    public enum Kind { RETRYABLE, PERMANENT, UNCERTAIN }
    private final Kind kind;
    public MailDeliveryFailure(Kind kind){super(kind.name());this.kind=kind;}
    public Kind kind(){return kind;}
}
