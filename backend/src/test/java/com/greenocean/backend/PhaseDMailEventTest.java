package com.greenocean.backend;
import com.greenocean.backend.auth.service.RegistrationService;
import com.greenocean.backend.auth.dto.RegisterRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
@TestPropertySource(properties="greenocean.mail.event-key=synthetic-mail-event-key-at-least-32-characters")
class PhaseDMailEventTest {
 @Autowired JdbcTemplate db;@Autowired RegistrationService registration;@Autowired MockMvc mvc;@Autowired ObjectMapper json;
 @Test @Tag("malicious") void authenticatedIdempotentEventsDoNotGrantEmailOwnershipOrOverwriteNewerOutcomes()throws Exception{
   String email="event-"+UUID.randomUUID()+"@example.test";registration.register(new RegisterRequest(email,"A synthetic mail event passphrase",null,null,null,"IR",null,null,true,1));
   UUID delivery=db.queryForObject("SELECT id FROM email_outbox WHERE recipient=?",UUID.class,email);db.update("UPDATE email_outbox SET status='SENT',payload='{}',delivery_state='ACCEPTED',provider_reference='qa-provider' WHERE id=?",delivery);
   Instant time=Instant.now();var event=Map.of("deliveryId",delivery,"providerReference","qa-provider","outcome","BOUNCED","occurredAt",time.toString());
   mvc.perform(post("/api/v1/mail/events").header("X-GreenOcean-Mail-Key","incorrect").contentType("application/json").content(json.writeValueAsString(event))).andExpect(status().isForbidden());
   for(int n=0;n<2;n++)mvc.perform(post("/api/v1/mail/events").header("X-GreenOcean-Mail-Key","synthetic-mail-event-key-at-least-32-characters").contentType("application/json").content(json.writeValueAsString(event))).andExpect(status().isNoContent());
   var earlier=Map.of("deliveryId",delivery,"providerReference","qa-provider","outcome","DELIVERED","occurredAt",time.minusSeconds(1).toString());
   mvc.perform(post("/api/v1/mail/events").header("X-GreenOcean-Mail-Key","synthetic-mail-event-key-at-least-32-characters").contentType("application/json").content(json.writeValueAsString(earlier))).andExpect(status().isNoContent());
   assertThat(db.queryForObject("SELECT delivery_state FROM email_outbox WHERE id=?",String.class,delivery)).isEqualTo("BOUNCED");assertThat(db.queryForObject("SELECT email_verified FROM users WHERE email=?",Boolean.class,email)).isFalse();
 }
}
