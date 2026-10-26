package com.greenocean.backend.assistance.service;
import java.util.UUID;
public interface MailTransport {
    void send(UUID deliveryId,String recipient,String template,String secret);
    record Receipt(String providerReference) {}
    default Receipt submit(UUID deliveryId,String sender,String recipient,String template,String secret,String language) {
        send(deliveryId,recipient,template,secret);return new Receipt(null);
    }
}
