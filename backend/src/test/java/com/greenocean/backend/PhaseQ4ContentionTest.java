package com.greenocean.backend;

import com.greenocean.backend.professional.service.*;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.trust.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties={"greenocean.clinic.commerce-enabled=true","greenocean.trust.enabled=true","spring.datasource.hikari.maximum-pool-size=10"})
@ActiveProfiles("test")
@org.springframework.test.annotation.DirtiesContext(classMode=org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class PhaseQ4ContentionTest extends MigrationTestSupport {
    static final String SCHEMA="qa_q4_race_"+UUID.randomUUID().toString().replace("-","");
    @DynamicPropertySource static void schema(DynamicPropertyRegistry p){
        String url=System.getenv("GREENOCEAN_TEST_DB_URL");
        p.add("spring.datasource.url",()->url+"?currentSchema="+SCHEMA+",public");
        p.add("spring.flyway.schemas",()->SCHEMA);p.add("spring.flyway.default-schema",()->SCHEMA);
        p.add("spring.jpa.properties.hibernate.default_schema",()->SCHEMA);
    }
    @Autowired JdbcTemplate db;
    @Autowired UserRepository users;
    @Autowired TokenService tokens;
    @Autowired ProfessionalScheduling scheduling;
    @Autowired ClinicCommerce commerce;
    @Autowired TrustAuthority authority;
    @Autowired PlatformTransactionManager transactions;
    @AfterAll static void drop(@Autowired JdbcTemplate db){db.execute("DROP SCHEMA "+SCHEMA+" CASCADE");}

    @Test void hundredContendingPatientsProduceExactlyOneBookingAndNoUnexpectedFailure()throws Exception {
        PhaseIFixtures.region(db);var pro=PhaseIFixtures.member(db,users,tokens);var staff=PhaseIFixtures.member(db,users,tokens);
        PhasePFixtures.qualified(db,pro.id(),staff.id());
        UUID policy=commerce.policy(staff.id(),new ClinicCommerce.Policy("q4-"+UUID.randomUUID(),0,0,
            "Synthetic free slot contention terms","Synthetic reviewed cancellation and refund policy"));
        UUID slot=scheduling.slot(pro.id(),new ProfessionalScheduling.Slot(Instant.now().plusSeconds(7200),Instant.now().plusSeconds(9000),"TEXT",0,"Asia/Tehran",true));
        commerce.offer(pro.id(),slot,policy,false);
        var patients=new ArrayList<UUID>();for(int i=0;i<100;i++)patients.add(PhaseIFixtures.member(db,users,tokens).id());
        var start=new CountDownLatch(1);var results=new ArrayList<Object>();
        try(var pool=Executors.newFixedThreadPool(100)){
            var pending=new ArrayList<Future<Object>>();
            for(UUID patient:patients)pending.add(pool.submit(()->{start.await();try{
                return commerce.book(patient,new ClinicCommerce.Booking(slot,UUID.randomUUID(),2,policy,0,true));
            }catch(RuntimeException failure){return failure;}}));
            start.countDown();for(var task:pending)results.add(task.get(45,TimeUnit.SECONDS));
        }
        assertThat(results.stream().filter(Map.class::isInstance).count()).isEqualTo(1);
        assertThat(results.stream().filter(ConflictException.class::isInstance).count()).isEqualTo(99);
        assertThat(db.queryForObject("SELECT count(*) FROM consultation_requests WHERE availability_slot_id=?",Integer.class,slot)).isEqualTo(1);
        assertThat(db.queryForObject("SELECT count(*) FROM clinic_appointment_events WHERE appointment_id IN(SELECT id FROM consultation_requests WHERE availability_slot_id=?)",Integer.class,slot)).isEqualTo(1);
    }

    @Test void timedOutQueryReturnsConnectionAndDoesNotPoisonNextTransaction()throws Exception {
        var source=db.getDataSource();assertThat(source).isNotNull();
        try(var connection=source.getConnection()){
            connection.setAutoCommit(false);
            try(var statement=connection.createStatement()){
                statement.execute("SET LOCAL statement_timeout='100ms'");
                assertThatThrownBy(()->statement.execute("SELECT pg_sleep(5)")).isInstanceOf(java.sql.SQLException.class);
            }finally{connection.rollback();}
        }
        assertThat(db.queryForObject("SELECT 1",Integer.class)).isEqualTo(1);
    }
    PhaseIFixtures.Actor operator(){
        var actor=PhaseIFixtures.member(db,users,tokens);
        db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='TRUST_ACCOUNT'",actor.id());
        db.update("UPDATE user_sessions SET mfa_authenticated_at=CURRENT_TIMESTAMP WHERE id=?",actor.session());return actor;
    }
    Object transaction(java.util.function.Supplier<?> action){
        try{return new TransactionTemplate(transactions).execute(state->action.get());}catch(RuntimeException failure){return failure;}
    }
    List<Object> race(java.util.function.Supplier<?> left,java.util.function.Supplier<?> right)throws Exception{
        try(var pool=Executors.newFixedThreadPool(2)){
            var start=new CountDownLatch(1);
            var a=pool.submit(()->{start.await();return transaction(left);});
            var b=pool.submit(()->{start.await();return transaction(right);});
            start.countDown();return List.of(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS));
        }
    }
    @Test void concurrentReplayRetriesPrepareOnlyOneImmutableRun()throws Exception {
        PhaseIFixtures.region(db);var actor=operator();String source="035879e196e99ed1ffc1097c3363acf7287040b0";
        String digest=RecoveryReplay.manifestDigest(source,List.of(),List.of());
        var replay=new RecoveryReplay(db,authority,UUID.randomUUID().toString(),digest,source);
        var results=race(()->replay.prepare(actor.id(),actor.session(),source,digest,List.of(),List.of()),
            ()->replay.prepare(actor.id(),actor.session(),source,digest,List.of(),List.of()));
        assertThat(results).allMatch(UUID.class::isInstance);assertThat(results.getFirst()).isEqualTo(results.getLast());
        assertThat(db.queryForObject("SELECT count(*) FROM recovery_replay_runs WHERE id=?",Integer.class,results.getFirst())).isEqualTo(1);
    }
    @Test void roleRemovalCannotRaceReviewIntoAnActiveRecoveryApproval()throws Exception {
        PhaseIFixtures.region(db);var first=operator();var second=operator();String source="035879e196e99ed1ffc1097c3363acf7287040b0";
        String digest=RecoveryReplay.manifestDigest(source,List.of(),List.of());
        var replay=new RecoveryReplay(db,authority,UUID.randomUUID().toString(),digest,source);
        UUID run=(UUID)transaction(()->replay.prepare(first.id(),first.session(),source,digest,List.of(),List.of()));
        var results=race(()->{replay.review(second.id(),second.session(),run);return "REVIEWED";},
            ()->{db.update("DELETE FROM user_roles WHERE user_id=? AND role_id=(SELECT id FROM roles WHERE name='TRUST_ACCOUNT')",second.id());return "REMOVED";});
        assertThat(results).contains("REMOVED");
        assertThat(results).allMatch(result->result instanceof String||result instanceof com.greenocean.backend.common.exception.UnauthorizedException||result instanceof com.greenocean.backend.common.exception.ForbiddenException);
        assertThat(replay.ready()).isFalse();
    }
}
