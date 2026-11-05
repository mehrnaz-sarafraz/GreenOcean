package com.greenocean.backend;
import com.greenocean.backend.trust.*;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import com.greenocean.backend.common.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.junit.jupiter.api.*;
import java.util.*;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties={"greenocean.trust.enabled=true","greenocean.pilot.enabled=true","greenocean.pilot.source-commit=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa","greenocean.pilot.owner-ids=00000000-0000-0000-0000-000000000101"})
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@ActiveProfiles("test") @Import(PhaseQ5PilotTest.Facts.class)
@org.springframework.test.annotation.DirtiesContext(classMode=org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class PhaseQ5PilotTest extends MigrationTestSupport {
    static final String SCHEMA="qa_q5_"+UUID.randomUUID().toString().replace("-","");
    static final String COMMIT="a".repeat(40);static final UUID OWNER=UUID.fromString("00000000-0000-0000-0000-000000000101");
    static final AtomicBoolean staffed=new AtomicBoolean(true);
    @DynamicPropertySource static void schema(DynamicPropertyRegistry p){String url=System.getenv("GREENOCEAN_TEST_DB_URL");p.add("spring.datasource.url",()->url+(url.contains("?")?"&":"?")+"currentSchema="+SCHEMA+",public");p.add("spring.flyway.schemas",()->SCHEMA);p.add("spring.flyway.default-schema",()->SCHEMA);p.add("spring.jpa.properties.hibernate.default_schema",()->SCHEMA);}
    /** All approval references and dependency facts in this isolated schema are QA simulations. */
    @TestConfiguration static class Facts {@Bean @Primary PilotDependencyChecks qaFacts(){return(s,c)->Map.of("STAFFING",staffed.get(),"MONITORING",true,"PROVIDERS",true,"REGION",true,"KEYS",true);}}
    @Autowired org.springframework.test.web.servlet.MockMvc http;
    @Autowired com.greenocean.backend.account.service.AccountService archives;
    @Autowired JdbcTemplate db;@Autowired UserRepository users;@Autowired TokenService tokens;
    @Autowired com.greenocean.backend.listener.live.LiveSupportService live;
    @Autowired com.greenocean.backend.listener.planning.ListenerQueue queue;
    @Autowired com.greenocean.backend.professional.service.ClinicCommerce commerce;
    @Autowired com.greenocean.backend.messaging.service.MessagingService messages;
    @Autowired com.greenocean.backend.social.service.SocialService social;
    @Autowired CurrentPilotDependencies actualDependencies;
    @Autowired PilotReadiness readiness;@Autowired PilotParticipants participants;@Autowired PilotGate gate;
    PhaseIFixtures.Actor author,reviewer,owner,member;
    PhaseIFixtures.Actor actor(UUID id,String... roles){
        db.update("INSERT INTO users(id,email,password_hash,status,email_verified,phase_a_enrolled) VALUES(?,?,'synthetic-only','ACTIVE',TRUE,TRUE) ON CONFLICT DO NOTHING",id,"q5-"+id+"@example.test");
        db.update("INSERT INTO profiles(user_id,username,display_name) VALUES(?,?,'Synthetic QA actor') ON CONFLICT DO NOTHING",id,"q5_"+id.toString().replace("-","").substring(0,20));
        db.update("INSERT INTO account_eligibility(user_id,age_result,age_method,residence_country,display_reviewed_at) VALUES(?,'ADULT','SELF_ATTESTED','IR',CURRENT_TIMESTAMP) ON CONFLICT DO NOTHING",id);
        db.update("INSERT INTO user_preferences(user_id) VALUES(?) ON CONFLICT DO NOTHING",id);
        for(String role:roles)db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name=? ON CONFLICT DO NOTHING",id,role);
        var a=PhaseIFixtures.session(db,users,tokens,id);db.update("UPDATE user_sessions SET mfa_authenticated_at=CURRENT_TIMESTAMP,reauthenticated_at=CURRENT_TIMESTAMP WHERE id=?",a.session());return a;
    }
    @BeforeEach void setup(){
        staffed.set(true);PhaseIFixtures.region(db);db.update("UPDATE pilot_participants SET state='REVOKED' WHERE state IN('INVITED','ACCEPTED')");
        author=actor(UUID.randomUUID(),"USER","TRUST_PLATFORM");reviewer=actor(UUID.randomUUID(),"USER","TRUST_PLATFORM");owner=actor(OWNER,"USER","TRUST_PLATFORM","PILOT_OWNER");member=PhaseIFixtures.member(db,users,tokens);
        db.update("UPDATE policy_documents SET status='PUBLISHED',published_at=CURRENT_TIMESTAMP,content='SYNTHETIC QA ONLY optional pilot notice; not real legal approval' WHERE policy_key='pilot-participation' AND version=1 AND status='DRAFT'");
    }
    @AfterAll static void drop(@Autowired JdbcTemplate db){db.execute("DROP SCHEMA "+SCHEMA+" CASCADE");}
    UUID assessment(){return readiness.create(author.id(),author.session(),"COMMUNITY","IR",COMMIT);}
    long revision(UUID id){return db.queryForObject("SELECT revision FROM pilot_assessments WHERE id=?",Long.class,id);}
    String kind(String criterion){return switch(criterion){case "CI"->"CI";case "SECURITY"->"INDEPENDENT_REVIEW";case "LEGAL"->"LEGAL_APPROVAL";case "DEVICES"->"PHYSICAL_DEVICE";case "BACKUP_RESTORE"->"STAGING_RESTORE";case "CAPACITY"->"CAPACITY_TEST";default->"OPERATIONS";};}
    UUID add(UUID id,String criterion,String kind){return readiness.evidence(author.id(),author.session(),id,new PilotReadiness.Evidence(revision(id),criterion,"PASS",kind,"QA_ONLY:"+UUID.randomUUID(),"b".repeat(64),List.of("QA_ONLY.fixture"),"QA_REVIEWER_NOT_REAL_APPROVAL",List.of(),Instant.now().plusSeconds(3600)));}
    UUID recommended(){UUID id=assessment();for(String criterion:PilotReadiness.CRITERIA){UUID e=add(id,criterion,kind(criterion));readiness.review(reviewer.id(),reviewer.session(),id,e,revision(id));}assertThat(state(id)).isEqualTo("GO_RECOMMENDED");return id;}
    UUID approved(){UUID id=recommended();readiness.approve(owner.id(),owner.session(),id,revision(id));return id;}
    Object state(UUID id){return readiness.assessment(id).get("state");}
    @Test void emptyAssessmentNeverActivatesAService(){UUID id=assessment();assertThat(state(id)).isEqualTo("NOT_ASSESSED");assertThat(gate.eligible(member.id(),"COMMUNITY")).isFalse();assertThat(db.queryForObject("SELECT count(*) FROM pilot_participants WHERE user_id=?",Integer.class,member.id())).isZero();}
    @Test void administratorHasNoImplicitAssessmentOrOwnerAuthority(){var admin=actor(UUID.randomUUID(),"USER","ADMIN");assertThatThrownBy(()->readiness.create(admin.id(),admin.session(),"COMMUNITY","IR",COMMIT)).isInstanceOf(ForbiddenException.class);assertThatThrownBy(()->readiness.approve(author.id(),author.session(),assessment(),1)).isInstanceOf(ForbiddenException.class);}
    @Test void selfReviewAndStaleRevisionAreRejected(){UUID id=assessment(),e=add(id,"CI","CI");assertThatThrownBy(()->readiness.review(author.id(),author.session(),id,e,revision(id))).isInstanceOf(ForbiddenException.class);assertThatThrownBy(()->readiness.review(reviewer.id(),reviewer.session(),id,e,1)).isInstanceOf(ConflictException.class);}
    @Test void syntheticEvidenceCannotCloseLaunchCriteria(){UUID id=assessment();for(String criterion:PilotReadiness.CRITERIA){UUID e=add(id,criterion,"SYNTHETIC");readiness.review(reviewer.id(),reviewer.session(),id,e,revision(id));}assertThat(state(id)).isNotIn("GO_RECOMMENDED","OWNER_APPROVED");assertThatThrownBy(()->readiness.approve(owner.id(),owner.session(),id,revision(id))).isInstanceOf(ConflictException.class);}
    @Test void ownerApprovalDoesNotEnrollVisitorsAndConsentIsRequired(){UUID id=approved();assertThat(state(id)).isEqualTo("OWNER_APPROVED");assertThat(gate.eligible(member.id(),"COMMUNITY")).isFalse();UUID invite=participants.invite(owner.id(),owner.session(),id,member.id());assertThat(gate.eligible(member.id(),"COMMUNITY")).isFalse();participants.choice(member.id(),invite,1,true);assertThat(gate.eligible(member.id(),"COMMUNITY")).isTrue();participants.choice(member.id(),invite,2,false);assertThat(gate.eligible(member.id(),"COMMUNITY")).isFalse();}
    @Test void anotherAccountCannotAcceptAnInvitation(){UUID id=approved(),invite=participants.invite(owner.id(),owner.session(),id,member.id());var other=PhaseIFixtures.member(db,users,tokens);assertThatThrownBy(()->participants.choice(other.id(),invite,1,true)).isInstanceOf(NotFoundException.class);}
    @Test void evidenceChangeInvalidatesExactOwnerApproval(){UUID id=approved();add(id,"CI","CI");assertThat(state(id)).isNotEqualTo("OWNER_APPROVED");}
    @Test void currentHumanCoverageLossImmediatelyBlocksAdmission(){UUID id=approved(),invite=participants.invite(owner.id(),owner.session(),id,member.id());participants.choice(member.id(),invite,1,true);staffed.set(false);assertThat(state(id)).isEqualTo("OPERATIONALLY_BLOCKED");assertThat(gate.eligible(member.id(),"COMMUNITY")).isFalse();}
    @Test void revokedAndRegrantedReviewerCannotReviveApproval(){UUID id=approved();db.update("DELETE FROM user_roles WHERE user_id=? AND role_id=(SELECT id FROM roles WHERE name='TRUST_PLATFORM')",reviewer.id());db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='TRUST_PLATFORM'",reviewer.id());assertThat(state(id)).isEqualTo("NO_GO");}
    @Test void currentNoticeChangeInvalidatesApprovalAndOldConsent(){UUID id=approved();db.update("INSERT INTO policy_documents(policy_key,version,title,summary,content,status,published_at) VALUES('pilot-participation',2,'QA change','Synthetic QA','A changed synthetic QA notice','PUBLISHED',CURRENT_TIMESTAMP)");assertThat(state(id)).isNotEqualTo("OWNER_APPROVED");}
    @Test void serviceAndCountryCannotBeChangedThroughParticipantParameters(){UUID id=approved(),invite=participants.invite(owner.id(),owner.session(),id,member.id());participants.choice(member.id(),invite,1,true);assertThat(gate.eligible(member.id(),"LISTENERS")).isFalse();db.update("UPDATE account_eligibility SET current_country='US' WHERE user_id=?",member.id());assertThat(gate.eligible(member.id(),"COMMUNITY")).isFalse();}
    @Test void explicitCohortCapDoesNotEnrollTheNextVisitor(){UUID id=approved();for(int i=0;i<100;i++){var target=PhaseIFixtures.member(db,users,tokens);participants.invite(owner.id(),owner.session(),id,target.id());}assertThatThrownBy(()->participants.invite(owner.id(),owner.session(),id,member.id())).isInstanceOf(ConflictException.class);assertThat(gate.eligible(member.id(),"COMMUNITY")).isFalse();}
    @Test void noActualEvidenceSessionIdentifiersAreExposed(){UUID id=assessment();add(id,"CI","CI");String out=readiness.detail(author.id(),author.session(),id).toString();assertThat(out).doesNotContain("author_session","created_session",author.session().toString());}
    @Test void actualHttpSecurityRequiresSessionPurposeAndOwnInvitation() throws Exception {
        http.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/pilot/me")).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        http.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/pilot/assessments").header("Authorization","Bearer "+member.token())).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        http.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/pilot/me").header("Authorization","Bearer "+member.token())).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        http.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/posts/feed?pilot=true&service=COMMUNITY").header("Authorization","Bearer "+member.token())).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        http.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/public/pilot-readiness?country=IR")).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }
    @Test void accountArchiveIncludesOnlyOwnPilotChoicesWithoutStaffSessions(){
        UUID id=approved(),invite=participants.invite(owner.id(),owner.session(),id,member.id());participants.choice(member.id(),invite,1,true);
        String own=archives.archive(member.id()).trustAndSecurity().toString();
        assertThat(own).contains(invite.toString(),"ACCEPTED").doesNotContain(owner.id().toString(),author.session().toString(),owner.session().toString());
        assertThat(archives.archive(reviewer.id()).trustAndSecurity().toString()).doesNotContain(invite.toString());
    }
    @Test void stickyEnforcementCannotBeDisabledByRemovingTheRuntimeFlag(){
        var stopped=new PilotGate(db,readiness,false);assertThat(stopped.enforced()).isTrue();assertThat(stopped.eligible(member.id(),"COMMUNITY")).isFalse();
    }
    @Test void stoppedAssessmentCannotBeRevivedByExistingOwnerApproval(){UUID id=approved();readiness.revoke(reviewer.id(),reviewer.session(),id);assertThat(state(id)).isEqualTo("NO_GO");assertThatThrownBy(()->participants.invite(owner.id(),owner.session(),id,member.id())).isInstanceOf(ConflictException.class);}

    @Test void sourceServicesIndependentlyRejectUnenrolledAdmissions(){
        assertThatThrownBy(()->live.create(member.id(),UUID.randomUUID(),null)).isInstanceOf(ForbiddenException.class).hasMessageContaining("pilot invitation");
        assertThatThrownBy(()->queue.join(member.id(),new com.greenocean.backend.listener.planning.ListenerQueue.Admission(UUID.randomUUID(),"en",false))).isInstanceOf(ForbiddenException.class).hasMessageContaining("pilot invitation");
        assertThatThrownBy(()->commerce.book(member.id(),new com.greenocean.backend.professional.service.ClinicCommerce.Booking(UUID.randomUUID(),UUID.randomUUID(),1,UUID.randomUUID(),0,true))).isInstanceOf(ForbiddenException.class).hasMessageContaining("pilot invitation");
    }

    @Test void actualDependenciesDoNotTreatQaMailOrDisabledAlertWorkerAsRealOperations(){
        var facts=actualDependencies.current("COMMUNITY","IR");assertThat(facts).containsEntry("PROVIDERS",false).containsEntry("MONITORING",false);
        assertThat(actualDependencies.current("CLINICAL_MEDIA","IR")).containsEntry("PROVIDERS",false).containsEntry("KEYS",false);
    }

    @Test void socialAndMessageAdmissionsCannotBypassThePilotThroughExistingServices(){
        assertThatThrownBy(()->social.follow(member.id(),author.id())).isInstanceOf(ForbiddenException.class).hasMessageContaining("pilot invitation");
        assertThatThrownBy(()->messages.startDirectConversation(author.id(),member.id())).isInstanceOf(ForbiddenException.class).hasMessageContaining("pilot invitation");
    }

    @Test void changedCountryBeforeConsentMustRejectAcceptance(){
        UUID id=approved(),invite=participants.invite(owner.id(),owner.session(),id,member.id());db.update("UPDATE account_eligibility SET current_country='US' WHERE user_id=?",member.id());
        assertThatThrownBy(()->participants.choice(member.id(),invite,1,true)).isInstanceOf(ForbiddenException.class);
        assertThat(db.queryForObject("SELECT state FROM pilot_participants WHERE id=?",String.class,invite)).isEqualTo("INVITED");
    }
    @Test void changedAgeBeforeConsentMustRejectAcceptance(){
        UUID id=approved(),invite=participants.invite(owner.id(),owner.session(),id,member.id());db.update("UPDATE account_eligibility SET age_result='UNKNOWN',age_method='UNKNOWN' WHERE user_id=?",member.id());
        assertThatThrownBy(()->participants.choice(member.id(),invite,1,true)).isInstanceOf(ForbiddenException.class);
        assertThat(db.queryForObject("SELECT state FROM pilot_participants WHERE id=?",String.class,invite)).isEqualTo("INVITED");
    }

}
