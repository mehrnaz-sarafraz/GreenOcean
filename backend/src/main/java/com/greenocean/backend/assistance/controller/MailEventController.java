package com.greenocean.backend.assistance.controller;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import com.greenocean.backend.common.exception.ForbiddenException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/** Optional trusted-gateway outcome callback; reports delivery, never changes account authority. */
@RestController
public class MailEventController {
    private final JdbcTemplate db;private final String key;
    public MailEventController(JdbcTemplate db,@Value("${greenocean.mail.event-key:}") String key){this.db=db;this.key=key;}
    public record Event(@NotNull UUID deliveryId,@Size(max=160) String providerReference,
                        @NotNull @Pattern(regexp="DELIVERED|BOUNCED") String outcome,@NotNull Instant occurredAt) {}
    @PostMapping("/api/v1/mail/events") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void receive(@RequestHeader(name="X-GreenOcean-Mail-Key",defaultValue="") String supplied,@Valid @RequestBody Event event) {
        if(key.length()<32||!java.security.MessageDigest.isEqual(key.getBytes(StandardCharsets.UTF_8),supplied.getBytes(StandardCharsets.UTF_8)))throw new ForbiddenException("Delivery event is not authorized");
        if(event.occurredAt().isAfter(Instant.now().plusSeconds(300))||event.occurredAt().isBefore(Instant.now().minusSeconds(604800)))throw new IllegalArgumentException("Delivery event timestamp is outside the accepted window");
        db.update("""
            UPDATE email_outbox SET delivery_state=?,delivery_event_at=? WHERE id=? AND status='SENT'
              AND (provider_reference IS NULL OR provider_reference=?)
              AND (delivery_event_at IS NULL OR delivery_event_at<?)
            """,event.outcome(),java.sql.Timestamp.from(event.occurredAt()),event.deliveryId(),event.providerReference(),java.sql.Timestamp.from(event.occurredAt()));
    }
}
