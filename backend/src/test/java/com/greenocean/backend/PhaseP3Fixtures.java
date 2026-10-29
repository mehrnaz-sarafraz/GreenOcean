package com.greenocean.backend;
import com.greenocean.backend.professional.service.*;import com.greenocean.backend.common.exception.*;
import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import org.springframework.test.context.ActiveProfiles;import org.springframework.transaction.annotation.Transactional;import org.junit.jupiter.api.*;import org.springframework.mock.web.MockMultipartFile;
import java.time.*;import java.util.*;import static org.assertj.core.api.Assertions.*;
abstract class PhaseP3Fixtures extends PhaseO3Fixtures {
 @Autowired ClinicalCare care;@Autowired ClinicalFiles files;@Autowired ClinicCommerce commerce;
 UUID appointment,encounter,policy;
 @BeforeEach void clinical(){PhasePFixtures.qualified(db,author.id(),issuer.id());role(author.id(),"PROFESSIONAL");role(reviewer.id(),"ADMIN");db.update("UPDATE user_sessions SET mfa_authenticated_at=CURRENT_TIMESTAMP,reauthenticated_at=CURRENT_TIMESTAMP WHERE user_id IN(?,?,?)",author.id(),issuer.id(),reviewer.id());
  policy=commerce.policy(issuer.id(),new ClinicCommerce.Policy("p3-"+UUID.randomUUID(),0,0,"Synthetic private encounter free service terms","Synthetic refund review terms for isolated QA"));
  UUID slot=db.queryForObject("INSERT INTO professional_availability_slots(id,professional_user_id,starts_at,ends_at,consultation_mode,status,governed) VALUES(uuidv7(),?,CURRENT_TIMESTAMP-INTERVAL '1 minute',CURRENT_TIMESTAMP+INTERVAL '29 minutes','TEXT','BOOKED',TRUE) RETURNING id",UUID.class,author.id());
  appointment=db.queryForObject("INSERT INTO consultation_requests(requester_user_id,professional_user_id,availability_slot_id,scheduled_at,ends_at,consultation_mode,status,clinic_governed,booking_key,price_units,commission_units,provider_fee_units,currency,policy_id,terms_snapshot,profession_snapshot,confirmed_at) SELECT ?,?,id,starts_at,ends_at,'TEXT','CONFIRMED',TRUE,?,0,0,0,'IRR',?,'Synthetic exact accepted free terms','PSYCHOLOGIST',CURRENT_TIMESTAMP FROM professional_availability_slots WHERE id=? RETURNING id",UUID.class,learner.id(),author.id(),UUID.randomUUID(),policy,slot);
  for(String category:List.of("MESSAGES","SHARED_NOTES","PROFESSIONAL_NOTES","ATTACHMENTS"))care.policy(issuer.id(),new ClinicalCare.RecordPolicy("qa-"+UUID.randomUUID(),category,category.equals("PROFESSIONAL_NOTES")?"REVIEW_REQUIRED":"DIRECT",7,null,"SYNTHETIC-QA-PROCESSING-POLICY-NOT-LEGAL-APPROVAL",Instant.now().plusSeconds(3600)));
  for(String kind:List.of("PLATFORM_TERMS","CARE_CONSENT","DATA_NOTICE")){UUID draft=care.draftNotice(issuer.id(),new ClinicalCare.Notice(kind,"qa-"+UUID.randomUUID(),"en","TEXT","Synthetic original clinical notice wording for isolated tests; not a real legal notice.",Instant.now().minusSeconds(60),Instant.now().plusSeconds(3600)));care.approveNotice(reviewer.id(),draft,1);}
 }
 void consent(){for(var n:care.notices(learner.id(),learner.session(),appointment,"en"))care.consent(learner.id(),learner.session(),appointment,(UUID)n.get("id"),UUID.randomUUID(),"ACCEPT");}
 UUID enter(){consent();UUID id=care.enter(learner.id(),learner.session(),appointment);assertThat(care.enter(author.id(),author.session(),appointment)).isEqualTo(id);return id;}
 UUID active(){UUID id=enter();care.start(author.id(),author.session(),id,1);return id;}
}
