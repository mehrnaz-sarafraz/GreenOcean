package com.greenocean.backend.professional.service;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import java.util.UUID;
/** Off until an approved operator, provider and operating process are configured.
 * Each attempt uses the durable obligation UUID at the provider; a timeout is uncertain, not success. */
@Component
public class ClinicRefundWorker {
 private final JdbcTemplate db;private final ClinicCommerce commerce;private final String operator;private final boolean enabled;
 public ClinicRefundWorker(JdbcTemplate db,ClinicCommerce commerce,@Value("${greenocean.clinic.refund-operator-id:}")String operator,@Value("${greenocean.clinic.refund-worker-enabled:false}")boolean enabled){this.db=db;this.commerce=commerce;this.operator=operator;this.enabled=enabled;}
 @Scheduled(fixedDelayString="${greenocean.clinic.refund-poll-ms:60000}") public void poll(){if(!enabled||operator.isBlank())return;UUID actor=UUID.fromString(operator);
  for(var row:db.queryForList("SELECT appointment_id FROM clinic_refunds WHERE state IN('REFUND_ELIGIBLE','REFUND_REQUESTED') ORDER BY initiate_by LIMIT 50")){UUID id=(UUID)row.get("appointment_id");try{commerce.requestRefund(actor,id);}catch(RuntimeException unavailable){db.update("UPDATE clinic_refunds SET state='MANUAL_REVIEW_REQUIRED',updated_at=CURRENT_TIMESTAMP WHERE appointment_id=? AND state IN('REFUND_ELIGIBLE','REFUND_REQUESTED')",id);db.update("INSERT INTO clinic_provider_attempts(appointment_id,operation,state) VALUES(?,'REFUND','MANUAL_REVIEW_REQUIRED')",id);}}
 }
}
