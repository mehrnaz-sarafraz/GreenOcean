package com.greenocean.backend;
import com.greenocean.backend.professional.service.*;
import com.greenocean.backend.common.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.*;
import java.time.*;import java.util.*;import java.util.concurrent.ConcurrentHashMap;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties="greenocean.clinic.commerce-enabled=true") @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseP2PaymentsTest extends PhaseO3Fixtures {
 @TestConfiguration static class Providers {@Bean SyntheticProvider syntheticProvider(){return new SyntheticProvider();}}
 /** Test-only fake. No production bean/profile creates fabricated payment success. */
 static class SyntheticProvider implements ClinicPaymentProvider {
  final Map<String,Evidence> evidence=new ConcurrentHashMap<>();boolean mismatch;
  public String name(){return "SYNTHETIC_QA_ONLY";}public boolean authorized(){return true;}
  public String create(Intent in){String ref="synthetic-charge-"+in.id();evidence.putIfAbsent(ref,new Evidence("event-"+in.id(),ref,"CAPTURE",in.units(),in.currency()));return ref;}
  public Evidence reconcile(String ref){var e=evidence.get(ref);return mismatch?new Evidence(e.eventKey(),e.reference(),e.kind(),e.units()+1,e.currency()):e;}
  public String refund(UUID id,String capture,long units,String currency){String ref="synthetic-refund-"+id;evidence.putIfAbsent(ref,new Evidence("refund-event-"+id,ref,"REFUND",units,currency));return ref;}
  public String verifyCallback(byte[] body,String signature){if(!"SYNTHETIC-VALID-SIGNATURE".equals(signature))throw new IllegalArgumentException("Synthetic invalid signature");return new String(body,java.nio.charset.StandardCharsets.UTF_8);}
 }
 @Autowired ClinicCommerce commerce;@Autowired ProfessionalScheduling scheduling;@Autowired SyntheticProvider provider;
 UUID id;
 @BeforeEach void clinic(){provider.mismatch=false;PhasePFixtures.qualified(db,author.id(),issuer.id());db.update("INSERT INTO clinic_release_approvals(country_code,mode,purpose,evidence_reference,approved_by,expires_at) VALUES('IR','TEXT','PAYMENT_PROVIDER','SYNTHETIC-TEST-NOT-REAL-PAYMENT-APPROVAL',?,CURRENT_TIMESTAMP+INTERVAL '1 day')",issuer.id());UUID policy=commerce.policy(issuer.id(),new ClinicCommerce.Policy("qa-"+UUID.randomUUID(),1000,100,"Synthetic transparent 10 percent commission and 100 rial provider fee","Synthetic provider cancellation full refund, patient cancellation reviewed"));UUID slot=scheduling.slot(author.id(),new ProfessionalScheduling.Slot(Instant.now().plusSeconds(7200),Instant.now().plusSeconds(9000),"TEXT",10000,"Asia/Tehran",true));commerce.offer(author.id(),slot,policy,false);id=(UUID)commerce.book(learner.id(),new ClinicCommerce.Booking(slot,UUID.randomUUID(),2,policy,10000,true)).get("id");}
 @Test void onlyExactProviderEvidenceConfirmsPaymentAndDuplicateReconciliationIsOnce(){assertThat(commerce.appointment(learner.id(),id).get("status")).isEqualTo("PAYMENT_PENDING");var intent=commerce.payment(learner.id(),id);assertThat(commerce.payment(learner.id(),id)).isEqualTo(intent);assertThat(commerce.appointment(learner.id(),id).get("status")).isEqualTo("PAYMENT_PENDING");commerce.reconcile(issuer.id(),id);commerce.reconcile(issuer.id(),id);assertThat(commerce.appointment(learner.id(),id).get("status")).isEqualTo("CONFIRMED");assertThat(db.queryForObject("SELECT count(*) FROM clinic_financial_events WHERE appointment_id=? AND kind='CAPTURE'",Integer.class,id)).isEqualTo(1);}
 @Test void mismatchedAmountIsNotCapture(){commerce.payment(learner.id(),id);provider.mismatch=true;assertThatThrownBy(()->commerce.reconcile(issuer.id(),id)).isInstanceOf(ConflictException.class);assertThat(db.queryForObject("SELECT count(*) FROM clinic_financial_events WHERE appointment_id=?",Integer.class,id)).isZero();}
 @Test void cancellationCreatesLiabilityAndOnlyProviderConfirmationFinishesRefund(){commerce.payment(learner.id(),id);commerce.reconcile(issuer.id(),id);commerce.change(author.id(),id,new ClinicCommerce.Change(2,"CANCEL","Synthetic professional cancellation"));assertThat(refund()).isEqualTo("REFUND_ELIGIBLE");commerce.requestRefund(issuer.id(),id);commerce.requestRefund(issuer.id(),id);assertThat(refund()).isEqualTo("REFUND_PROCESSING");commerce.reconcileRefund(issuer.id(),id);commerce.reconcileRefund(issuer.id(),id);assertThat(refund()).isEqualTo("REFUND_CONFIRMED");assertThat(db.queryForObject("SELECT count(*) FROM clinic_financial_events WHERE appointment_id=? AND kind='REFUND_CONFIRMED'",Integer.class,id)).isEqualTo(1);}
 @Test void foreignPatientCannotCreateIntentOrRefund(){assertThatThrownBy(()->commerce.payment(other.id(),id)).isInstanceOf(NotFoundException.class);assertThatThrownBy(()->commerce.requestRefund(learner.id(),id)).isInstanceOf(ForbiddenException.class);}
 @Test void forgedCallbackAndReplayCannotCreateExtraCapture(){String ref=(String)commerce.payment(learner.id(),id).get("reference");byte[] body=ref.getBytes(java.nio.charset.StandardCharsets.UTF_8);assertThatThrownBy(()->commerce.callback(body,"FORGED-SIGNATURE")).isInstanceOf(ForbiddenException.class);assertThat(commerce.appointment(learner.id(),id).get("status")).isEqualTo("PAYMENT_PENDING");commerce.callback(body,"SYNTHETIC-VALID-SIGNATURE");commerce.callback(body,"SYNTHETIC-VALID-SIGNATURE");assertThat(db.queryForObject("SELECT count(*) FROM clinic_financial_events WHERE appointment_id=? AND kind='CAPTURE'",Integer.class,id)).isEqualTo(1);}
 @Test void paidFeesAndTermsNeverFollowLaterSlotPrice(){var c=commerce.appointment(learner.id(),id);assertThat(c.get("commission_units")).isEqualTo(1000L);assertThat(c.get("provider_fee_units")).isEqualTo(100L);db.update("UPDATE professional_availability_slots SET price_units=20000 WHERE id=?",c.get("availability_slot_id"));assertThat(commerce.appointment(learner.id(),id).get("price_units")).isEqualTo(10000L);}
 @Test void lateCaptureCreatesRefundInsteadOfRevivingExpiredReservation(){commerce.payment(learner.id(),id);db.update("UPDATE consultation_requests SET payment_deadline=CURRENT_TIMESTAMP-INTERVAL '1 second' WHERE id=?",id);commerce.expireReservations(issuer.id());commerce.reconcile(issuer.id(),id);assertThat(commerce.appointment(learner.id(),id).get("status")).isEqualTo("CANCELLED_BY_PLATFORM");assertThat(refund()).isEqualTo("REFUND_ELIGIBLE");}
 @Test void patientDisputeDoesNotInventRefundEvidence(){commerce.payment(learner.id(),id);commerce.reconcile(issuer.id(),id);commerce.dispute(learner.id(),id,"Synthetic dispute with request for refund review");assertThat(db.queryForObject("SELECT count(*) FROM clinic_refunds WHERE appointment_id=?",Integer.class,id)).isZero();commerce.resolve(issuer.id(),id,true,"Synthetic independently reviewed refund decision");assertThat(refund()).isEqualTo("REFUND_ELIGIBLE");}
 String refund(){return db.queryForObject("SELECT state FROM clinic_refunds WHERE appointment_id=?",String.class,id);}
}
