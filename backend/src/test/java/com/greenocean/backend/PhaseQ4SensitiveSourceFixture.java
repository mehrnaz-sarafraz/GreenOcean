package com.greenocean.backend;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import com.greenocean.backend.professional.service.*;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.Instant;
import java.util.*;

/** Fresh synthetic private content before a backup; never seeds real clinical processing. */
public final class PhaseQ4SensitiveSourceFixture {
    public static void main(String[] args){
        String url=System.getenv("GREENOCEAN_TEST_DB_URL");
        if(!"isolated-only".equals(System.getenv("GREENOCEAN_RESTORE_QA"))||!"jdbc:postgresql://127.0.0.1:55432/greenocean_q4_load_qa".equals(url))
            throw new IllegalStateException("Explicit source QA fixture required");
        try(var ctx=new SpringApplicationBuilder(BackendApplication.class).profiles("test").run(
            "--server.address=127.0.0.1","--server.port=0","--spring.datasource.url="+url,"--spring.datasource.username=postgres","--spring.datasource.password=",
            "--greenocean.clinic.commerce-enabled=true","--greenocean.clinic.records-enabled=true",
            "--greenocean.clinic.record-keys=clinical-qa:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","--greenocean.clinic.record-current-key=clinical-qa")){
            var db=ctx.getBean(JdbcTemplate.class);var users=ctx.getBean(UserRepository.class);var tokens=ctx.getBean(TokenService.class);PhaseIFixtures.region(db);
            var patient=PhaseIFixtures.member(db,users,tokens);var professional=PhaseIFixtures.member(db,users,tokens);
            var issuer=PhaseIFixtures.member(db,users,tokens);var reviewer=PhaseIFixtures.member(db,users,tokens);
            for(var actor:List.of(issuer,reviewer))db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='ADMIN'",actor.id());
            db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='PROFESSIONAL'",professional.id());
            PhasePFixtures.qualified(db,professional.id(),issuer.id());
            for(var actor:List.of(patient,professional,issuer,reviewer))db.update("UPDATE user_sessions SET mfa_authenticated_at=CURRENT_TIMESTAMP,reauthenticated_at=CURRENT_TIMESTAMP WHERE id=?",actor.session());
            var commerce=ctx.getBean(ClinicCommerce.class);var care=ctx.getBean(ClinicalCare.class);
            UUID policy=commerce.policy(issuer.id(),new ClinicCommerce.Policy("q4-restore-"+UUID.randomUUID(),0,0,"Synthetic backup encounter terms","Synthetic backup cancellation terms"));
            UUID slot=db.queryForObject("INSERT INTO professional_availability_slots(id,professional_user_id,starts_at,ends_at,consultation_mode,status,governed) VALUES(uuidv7(),?,CURRENT_TIMESTAMP-INTERVAL '1 minute',CURRENT_TIMESTAMP+INTERVAL '29 minutes','TEXT','BOOKED',TRUE) RETURNING id",UUID.class,professional.id());
            UUID appointment=db.queryForObject("INSERT INTO consultation_requests(requester_user_id,professional_user_id,availability_slot_id,scheduled_at,ends_at,consultation_mode,status,clinic_governed,booking_key,price_units,commission_units,provider_fee_units,currency,policy_id,terms_snapshot,profession_snapshot,confirmed_at) SELECT ?,?,id,starts_at,ends_at,'TEXT','CONFIRMED',TRUE,uuidv7(),0,0,0,'IRR',?,'Synthetic accepted backup terms','PSYCHOLOGIST',CURRENT_TIMESTAMP FROM professional_availability_slots WHERE id=? RETURNING id",UUID.class,patient.id(),professional.id(),policy,slot);
            for(String category:List.of("MESSAGES","SHARED_NOTES","PROFESSIONAL_NOTES","ATTACHMENTS"))care.policy(issuer.id(),new ClinicalCare.RecordPolicy("q4-restore-"+UUID.randomUUID(),category,category.equals("PROFESSIONAL_NOTES")?"REVIEW_REQUIRED":"DIRECT",7,null,"SYNTHETIC-RESTORE-NOT-LEGAL-APPROVAL",Instant.now().plusSeconds(3600)));
            for(String kind:List.of("PLATFORM_TERMS","CARE_CONSENT","DATA_NOTICE")){
                UUID notice=care.draftNotice(issuer.id(),new ClinicalCare.Notice(kind,"q4-restore-"+UUID.randomUUID(),"en","TEXT","Synthetic restore notice, not a real legal notice or clinical approval.",Instant.now().minusSeconds(60),Instant.now().plusSeconds(3600)));
                care.approveNotice(reviewer.id(),notice,1);
            }
            for(var notice:care.notices(patient.id(),patient.session(),appointment,"en"))care.consent(patient.id(),patient.session(),appointment,(UUID)notice.get("id"),UUID.randomUUID(),"ACCEPT");
            UUID encounter=care.enter(patient.id(),patient.session(),appointment);care.enter(professional.id(),professional.session(),appointment);care.start(professional.id(),professional.session(),encounter,1);
            care.message(patient.id(),patient.session(),encounter,UUID.randomUUID(),"SYNTHETIC-Q4-RESTORED-PRIVATE-CLINICAL-MESSAGE");
            care.message(professional.id(),professional.session(),encounter,UUID.randomUUID(),"SYNTHETIC-Q4-RESTORED-PRIVATE-PROFESSIONAL-REPLY");
            var writing=ctx.getBean(com.greenocean.backend.wellbeing.service.PrivateWellbeing.class);
            for(int i=0;i<3;i++){
                var actor=PhaseIFixtures.member(db,users,tokens);
                writing.write(actor.id(),null,new com.greenocean.backend.wellbeing.service.PrivateWellbeing.Writing("Synthetic backup entry","SYNTHETIC-Q4-RESTORED-PRIVATE-JOURNAL","",List.of(),0,UUID.randomUUID()));
            }
            System.out.println("Q4 source fixture created: two encrypted synthetic clinical messages and three fresh private journals");
        }
    }
}
