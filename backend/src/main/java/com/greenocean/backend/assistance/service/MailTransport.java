package com.greenocean.backend.assistance.service;
import java.util.UUID;
public interface MailTransport {
    void send(UUID deliveryId,String recipient,String template,String secret);
}
