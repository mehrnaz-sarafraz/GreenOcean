package com.greenocean.backend;

import com.greenocean.backend.trust.*;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties="greenocean.trust.enabled=true") @AutoConfigureMockMvc @ActiveProfiles("test")
@org.springframework.test.annotation.DirtiesContext(classMode=org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class PhaseQ1ConcurrencyTest extends MigrationTestSupport {
 static final String SCHEMA="qa_q1_race_"+UUID.randomUUID().toString().replace("-","");
 @DynamicPropertySource static void schema(DynamicPropertyRegistry p){String url=System.getenv("GREENOCEAN_TEST_DB_URL");p.add("spring.datasource.url",()->url+(url.contains("?")?"&":"?")+"currentSchema="+SCHEMA+",public");p.add("spring.flyway.schemas",()->SCHEMA);p.add("spring.flyway.default-schema",()->SCHEMA);p.add("spring.jpa.properties.hibernate.default_schema",()->SCHEMA);}
 @Autowired JdbcTemplate db;@Autowired UserRepository users;@Autowired TokenService tokens;@Autowired TrustCases cases;@Autowired TrustCoverage coverage;
 @AfterAll static void drop(@Autowired JdbcTemplate db){db.execute("DROP SCHEMA "+SCHEMA+" CASCADE");}
 PhaseIFixtures.Actor actor(String... roles){var a=PhaseIFixtures.member(db,users,tokens);for(String role:roles)db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name=? ON CONFLICT DO NOTHING",a.id(),role);db.update("UPDATE user_sessions SET mfa_authenticated_at=CURRENT_TIMESTAMP WHERE id=?",a.session());return a;}
 UUID source(){var reporter=actor();var target=actor();return db.queryForObject("INSERT INTO reports(reporter_id,target_user_id,reason) VALUES(?,?,'OTHER') RETURNING id",UUID.class,reporter.id(),target.id());}
 @BeforeEach void region(){db.update("INSERT INTO regional_services(country_code,service,status) VALUES('IR','COMMUNITY','AVAILABLE') ON CONFLICT(country_code,service) DO UPDATE SET status='AVAILABLE'");}
 List<Object> race(Supplier<?> a,Supplier<?> b)throws Exception{try(var pool=Executors.newFixedThreadPool(2)){var start=new CountDownLatch(1);var x=pool.submit(()->{start.await();try{return a.get();}catch(RuntimeException ex){return ex;}});var y=pool.submit(()->{start.await();try{return b.get();}catch(RuntimeException ex){return ex;}});start.countDown();return List.of(x.get(20,TimeUnit.SECONDS),y.get(20,TimeUnit.SECONDS));}}
 Object move(PhaseIFixtures.Actor a,UUID id,long rev,String state){cases.move(a.id(),a.session(),id,"SOCIAL",rev,state);return "COMMITTED";}
 @Test void concurrentSourceImportsProduceOneCaseAndOneInitialHistory()throws Exception{var a=actor("TRUST_SOCIAL");UUID source=source();var r=race(()->cases.coordinate(a.id(),a.session(),"REPORT",source,"SOCIAL"),()->cases.coordinate(a.id(),a.session(),"REPORT",source,"SOCIAL"));assertThat(r).allMatch(UUID.class::isInstance);assertThat(r.getFirst()).isEqualTo(r.getLast());assertThat(db.queryForObject("SELECT count(*) FROM trust_case_events WHERE case_id=?",Integer.class,r.getFirst())).isEqualTo(1);}
 @Test void staleConcurrentTransitionsHaveOneWinner()throws Exception{var a=actor("TRUST_SOCIAL");UUID c=cases.coordinate(a.id(),a.session(),"REPORT",source(),"SOCIAL");var r=race(()->move(a,c,1,"TRIAGED"),()->move(a,c,1,"WAITING_FOR_OPERATOR"));assertThat(r.stream().filter("COMMITTED"::equals).count()).isEqualTo(1);assertThat(db.queryForObject("SELECT revision FROM trust_cases WHERE id=?",Long.class,c)).isEqualTo(2);assertThat(db.queryForObject("SELECT count(*) FROM trust_case_events WHERE case_id=?",Integer.class,c)).isEqualTo(2);}
 @Test void simultaneousAssignmentsCannotOverbookAcknowledgedHumanCapacity()throws Exception{var a=actor("TRUST_SOCIAL");var manager=actor("TRUST_SOCIAL","TRUST_PLATFORM");UUID shift=coverage.schedule(manager.id(),manager.session(),"SOCIAL",a.id(),Instant.now().minusSeconds(10),Instant.now().plusSeconds(3600),1);coverage.acknowledge(a.id(),a.session(),shift);UUID c=cases.coordinate(a.id(),a.session(),"REPORT",source(),"SOCIAL"),d=cases.coordinate(a.id(),a.session(),"REPORT",source(),"SOCIAL");move(a,c,1,"TRIAGED");move(a,d,1,"TRIAGED");var r=race(()->move(a,c,2,"UNDER_REVIEW"),()->move(a,d,2,"UNDER_REVIEW"));assertThat(r.stream().filter("COMMITTED"::equals).count()).isEqualTo(1);assertThat(db.queryForObject("SELECT count(*) FROM trust_cases WHERE assignee_id=? AND state='UNDER_REVIEW'",Integer.class,a.id())).isEqualTo(1);}
}
