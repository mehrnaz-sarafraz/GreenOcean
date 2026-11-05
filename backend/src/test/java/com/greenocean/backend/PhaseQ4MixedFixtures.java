package com.greenocean.backend;

import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import com.greenocean.backend.professional.service.*;
import com.greenocean.backend.trust.TrustCases;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.Instant;
import java.util.*;

/** Synthetic purpose actors and current episode; never packaged as a production import path. */
final class PhaseQ4MixedFixtures {
    final ApplicationContext ctx;final JdbcTemplate db;final UserRepository users;final TokenService tokens;
    final List<PhaseIFixtures.Actor> actors;final PhaseIFixtures.Actor professional,issuer,reviewer;
    final UUID policy,group,encounter;final List<String> loginEmails;
    final Instant calendarStart=Instant.now().plusSeconds(7200);
    PhaseQ4MixedFixtures(ApplicationContext ctx,List<PhaseIFixtures.Actor> actors){
        this.ctx=ctx;this.actors=actors;db=ctx.getBean(JdbcTemplate.class);users=ctx.getBean(UserRepository.class);tokens=ctx.getBean(TokenService.class);
        issuer=operator("ADMIN");reviewer=operator("ADMIN");professional=operator("PROFESSIONAL");
        PhasePFixtures.qualified(db,professional.id(),issuer.id());
        db.update("INSERT INTO clinic_release_approvals(country_code,mode,purpose,evidence_reference,approved_by,expires_at) VALUES('IR','TEXT','PAYMENT_PROVIDER','SYNTHETIC-Q4-NOT-REAL-PAYMENT-APPROVAL',?,CURRENT_TIMESTAMP+INTERVAL '1 hour')",issuer.id());
        var commerce=ctx.getBean(ClinicCommerce.class);
        policy=commerce.policy(issuer.id(),new ClinicCommerce.Policy("q4-load-"+UUID.randomUUID(),1000,100,
            "Synthetic isolated load terms, integer IRR amounts","Synthetic full refund and cancellation review terms"));
        var staff=actors.getFirst();role(staff.id(),"TRUST_SOCIAL");
        var fresh=PhaseIFixtures.session(db,users,tokens,staff.id());strong(fresh);actors.set(0,fresh);
        UUID source=db.queryForObject("INSERT INTO reports(reporter_id,target_user_id,reason,description) VALUES(?,?,'OTHER','SYNTHETIC-Q4-PRIVATE-SOURCE') RETURNING id",UUID.class,actors.get(1).id(),actors.get(2).id());
        ctx.getBean(TrustCases.class).coordinate(fresh.id(),fresh.session(),"REPORT",source,"SOCIAL");
        group=db.queryForObject("INSERT INTO communities(name,slug,privacy_type,created_by) VALUES(?,?,'PUBLIC',?) RETURNING id",UUID.class,
            "Synthetic Q4 group "+UUID.randomUUID(),"q4-"+UUID.randomUUID(),issuer.id());
        db.update("INSERT INTO community_members(community_id,user_id,role,rules_version_ack) VALUES(?,?,'OWNER',1)",group,issuer.id());
        db.update("INSERT INTO community_rules(community_id,position,rule_text) VALUES(?,1,'Synthetic privacy and respectful support rule')",group);
        var patient=actors.get(11);strong(patient);
        UUID slot=db.queryForObject("INSERT INTO professional_availability_slots(id,professional_user_id,starts_at,ends_at,consultation_mode,status,governed) VALUES(uuidv7(),?,CURRENT_TIMESTAMP-INTERVAL '1 minute',CURRENT_TIMESTAMP+INTERVAL '29 minutes','TEXT','BOOKED',TRUE) RETURNING id",UUID.class,professional.id());
        UUID appointment=db.queryForObject("INSERT INTO consultation_requests(requester_user_id,professional_user_id,availability_slot_id,scheduled_at,ends_at,consultation_mode,status,clinic_governed,booking_key,price_units,commission_units,provider_fee_units,currency,policy_id,terms_snapshot,profession_snapshot,confirmed_at) SELECT ?,?,id,starts_at,ends_at,'TEXT','CONFIRMED',TRUE,uuidv7(),0,0,0,'IRR',?,'Synthetic current episode terms','PSYCHOLOGIST',CURRENT_TIMESTAMP FROM professional_availability_slots WHERE id=? RETURNING id",UUID.class,patient.id(),professional.id(),policy,slot);
        var care=ctx.getBean(ClinicalCare.class);
        for(String category:List.of("MESSAGES","SHARED_NOTES","PROFESSIONAL_NOTES","ATTACHMENTS"))
            care.policy(issuer.id(),new ClinicalCare.RecordPolicy("q4-"+UUID.randomUUID(),category,category.equals("PROFESSIONAL_NOTES")?"REVIEW_REQUIRED":"DIRECT",7,null,"SYNTHETIC-Q4-POLICY-NOT-LEGAL-APPROVAL",Instant.now().plusSeconds(3600)));
        for(String kind:List.of("PLATFORM_TERMS","CARE_CONSENT","DATA_NOTICE")){
            UUID notice=care.draftNotice(issuer.id(),new ClinicalCare.Notice(kind,"q4-"+UUID.randomUUID(),"en","TEXT",
                "Synthetic isolated clinical notice wording; not an approved real clinical or legal notice.",Instant.now().minusSeconds(60),Instant.now().plusSeconds(3600)));
            care.approveNotice(reviewer.id(),notice,1);
        }
        for(var notice:care.notices(patient.id(),patient.session(),appointment,"en"))
            care.consent(patient.id(),patient.session(),appointment,(UUID)notice.get("id"),UUID.randomUUID(),"ACCEPT");
        encounter=care.enter(patient.id(),patient.session(),appointment);
        care.enter(professional.id(),professional.session(),appointment);care.start(professional.id(),professional.session(),encounter,1);
        // Separate source-cohort accounts prevent login's normal session cap invalidating measured users.
        loginEmails=new ArrayList<>(db.queryForList("SELECT u.email FROM users u WHERE u.email LIKE 'i-%@example.test' AND u.status='ACTIVE' AND u.id NOT IN(SELECT unnest(?::uuid[])) AND NOT EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=u.id AND r.name<>'USER') ORDER BY u.created_at LIMIT 100",String.class,
            (Object)actors.stream().map(PhaseIFixtures.Actor::id).toArray(UUID[]::new)));
        while(loginEmails.size()<100){var account=PhaseIFixtures.member(db,users,tokens);loginEmails.add("i-"+account.id()+"@example.test");}
        String hash=ctx.getBean(org.springframework.security.crypto.password.PasswordEncoder.class).encode("Synthetic Q4 passphrase 2026");
        for(String email:loginEmails)db.update("UPDATE users SET password_hash=? WHERE email=?",hash,email);
    }
    PhaseIFixtures.Actor operator(String purpose){var actor=PhaseIFixtures.member(db,users,tokens);role(actor.id(),purpose);
        var fresh=PhaseIFixtures.session(db,users,tokens,actor.id());strong(fresh);return fresh;}
    void role(UUID who,String name){db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name=? ON CONFLICT DO NOTHING",who,name);}
    void strong(PhaseIFixtures.Actor actor){db.update("UPDATE user_sessions SET mfa_authenticated_at=CURRENT_TIMESTAMP,reauthenticated_at=CURRENT_TIMESTAMP WHERE id=?",actor.session());}
    List<List<PhaseQ4LoadRunner.Work>> round(List<PhaseQ4LoadRunner.Work> baseline,int round){
        var commerce=ctx.getBean(ClinicCommerce.class);var scheduling=ctx.getBean(ProfessionalScheduling.class);
        var output=new ArrayList<List<PhaseQ4LoadRunner.Work>>();
        for(int i=0;i<actors.size();i++){
            var work=new ArrayList<>(baseline);
            work.add(new PhaseQ4LoadRunner.Work("registration","POST","/api/v1/auth/register","{\"email\":\"q4-LOAD_NONCE@example.test\",\"password\":\"Synthetic Q4 passphrase 2026\",\"countryCode\":\"IR\",\"adultSelfAttested\":true,\"termsVersion\":1}"));
            work.add(new PhaseQ4LoadRunner.Work("login","POST","/api/v1/auth/login","{\"email\":\""+loginEmails.get(i)+"\",\"password\":\"Synthetic Q4 passphrase 2026\"}"));
            work.add(new PhaseQ4LoadRunner.Work("group-membership","PUT","/api/v1/communities/"+group+"/membership","{\"rulesVersion\":1}"));
            if(i<10){
                Instant start=calendarStart.plusSeconds(round*18000L+i*1800L);
                UUID free=scheduling.slot(professional.id(),new ProfessionalScheduling.Slot(start,start.plusSeconds(900),"TEXT",0,"UTC",true));
                commerce.offer(professional.id(),free,policy,false);UUID key=UUID.randomUUID();
                work.add(new PhaseQ4LoadRunner.Work("appointment-booking","POST","/api/v1/clinic/appointments",
                    "{\"slotId\":\""+free+"\",\"retryKey\":\""+key+"\",\"slotRevision\":2,\"policyId\":\""+policy+"\",\"expectedUnits\":0,\"confirmed\":true}"));
                Instant paidStart=start.plusSeconds(900);
                UUID paid=scheduling.slot(professional.id(),new ProfessionalScheduling.Slot(paidStart,paidStart.plusSeconds(900),"TEXT",10000,"UTC",true));
                commerce.offer(professional.id(),paid,policy,false);
                UUID appointment=(UUID)commerce.book(actors.get(i).id(),new ClinicCommerce.Booking(paid,UUID.randomUUID(),2,policy,10000,true)).get("id");
                String reference=(String)commerce.payment(actors.get(i).id(),appointment).get("reference");
                work.add(new PhaseQ4LoadRunner.Work("payment-callback","POST","/api/v1/clinic/provider-callback",reference));
            }
            if(i==0)work.add(new PhaseQ4LoadRunner.Work("staff-case","GET","/api/v1/trust/cases?domain=SOCIAL",null));
            if(i==11)work.add(new PhaseQ4LoadRunner.Work("clinical-signaling","GET","/api/v1/clinical/encounters/"+encounter,null));
            output.add(List.copyOf(work));
        }
        return output;
    }
}
