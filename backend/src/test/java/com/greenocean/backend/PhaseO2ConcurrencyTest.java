package com.greenocean.backend;
import com.greenocean.backend.catalog.service.GrowthLibrary;
import com.greenocean.backend.account.repository.AccountRepository;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.junit.jupiter.api.*;
import java.time.*;import java.util.*;import java.util.concurrent.*;import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest @ActiveProfiles("test") @org.springframework.test.annotation.DirtiesContext(classMode=org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class PhaseO2ConcurrencyTest extends MigrationTestSupport {
 static final String SCHEMA="qa_o2_race_"+UUID.randomUUID().toString().replace("-","");
 @org.springframework.test.context.DynamicPropertySource static void schema(org.springframework.test.context.DynamicPropertyRegistry p){String url=System.getenv("GREENOCEAN_TEST_DB_URL");p.add("spring.datasource.url",()->url+(url.contains("?")?"&":"?")+"currentSchema="+SCHEMA+",public");p.add("spring.flyway.schemas",()->SCHEMA);p.add("spring.flyway.default-schema",()->SCHEMA);p.add("spring.jpa.properties.hibernate.default_schema",()->SCHEMA);}
 @AfterAll static void drop(@Autowired JdbcTemplate db){db.execute("DROP SCHEMA "+SCHEMA+" CASCADE");}
 @Autowired JdbcTemplate db;@Autowired GrowthLibrary s;@Autowired UserRepository users;@Autowired TokenService tokens;@Autowired AccountRepository accounts;@Autowired PlatformTransactionManager manager;
 PhaseIFixtures.Actor author,reviewer,admin;
 Map<String,Object> doc(String title){var d=new LinkedHashMap<String,Object>();d.put("kind","ARTICLE");d.put("title",title);d.put("summary","Synthetic independent-connection exercise");d.put("body","Original synthetic educational material about small manageable actions; not a psychological assessment.");d.put("language","en");d.put("scope","GENERAL");d.put("topic","Planning");d.put("why","General education");d.put("audience","ADULT");d.put("rights","Original synthetic QA text");d.put("sources",List.of("https://example.org/source"));return d;}

 @BeforeEach void setup(){PhaseIFixtures.region(db);author=PhaseIFixtures.member(db,users,tokens);reviewer=PhaseIFixtures.member(db,users,tokens);admin=PhaseIFixtures.member(db,users,tokens);for(var pair:List.of(Map.entry(admin.id(),"ADMIN"),Map.entry(reviewer.id(),"MODERATOR")))db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name=? ON CONFLICT DO NOTHING",pair.getKey(),pair.getValue());for(String action:List.of("DRAFT","SUBMIT"))s.grant(admin.id(),new GrowthLibrary.Grant(author.id(),action,"GENERAL","Synthetic QA grant",Instant.now().plusSeconds(3600)));for(String action:List.of("REVIEW","APPROVE","PUBLISH"))s.grant(admin.id(),new GrowthLibrary.Grant(reviewer.id(),action,"GENERAL","Synthetic QA grant",Instant.now().plusSeconds(3600)));}
 @AfterEach void clean(){for(var actor:List.of(author,reviewer,admin))new TransactionTemplate(manager).executeWithoutResult(tx->accounts.erase(actor.id()));}
 Object result(Supplier<?> work){try{return work.get();}catch(RuntimeException error){return error;}}
 List<Object> race(Supplier<?> a,Supplier<?> b)throws Exception{try(var pool=Executors.newFixedThreadPool(2)){var go=new CountDownLatch(1);var x=pool.submit(()->{go.await();return result(a);});var y=pool.submit(()->{go.await();return result(b);});go.countDown();return List.of(x.get(20,TimeUnit.SECONDS),y.get(20,TimeUnit.SECONDS));}}
 UUID create(){return(UUID)s.create(author.id(),new GrowthLibrary.Draft(doc("Initial draft"),0,0)).get("resource_id");}
 @Test @Tag("creative") void twoIndependentDraftEditsHaveOneCommittedWinner()throws Exception{UUID id=create();var out=race(()->s.edit(author.id(),id,new GrowthLibrary.Draft(doc("First device"),1,0),false),()->s.edit(author.id(),id,new GrowthLibrary.Draft(doc("Second device"),1,0),false));assertThat(out.stream().filter(Map.class::isInstance).count()).isEqualTo(1);assertThat(out.stream().filter(com.greenocean.backend.common.exception.ConflictException.class::isInstance).count()).isEqualTo(1);assertThat(s.editorial(author.id(),id).get("etag")).isEqualTo(1L);}
 @Test @Tag("malicious") void forkRacingIndependentApprovalCannotApproveReplacementText()throws Exception{UUID id=create();s.transition(author.id(),id,new GrowthLibrary.Decision(1,0,"SUBMIT","Synthetic submission",false,false));race(()->s.edit(author.id(),id,new GrowthLibrary.Draft(doc("Unreviewed replacement"),1,1),true),()->s.transition(reviewer.id(),id,new GrowthLibrary.Decision(1,1,"APPROVE","Synthetic independent source/rights check",true,true)));var current=s.editorial(author.id(),id);if(((Number)current.get("revision")).intValue()==2)assertThat(current.get("state")).isEqualTo("DRAFT");else{assertThat(current.get("state")).isEqualTo("APPROVED");assertThat(((Map<?,?>)current.get("document")).get("title")).isEqualTo("Initial draft");}assertThat(db.queryForObject("SELECT count(*) FROM growth_resource_versions WHERE resource_id=? AND state='PUBLISHED'",Integer.class,id)).isZero();}
 @Test @Tag("malicious") void erasureRacingNewSubmissionLeavesNoPrivateAuthorDraft()throws Exception{race(()->s.create(author.id(),new GrowthLibrary.Draft(doc("Draft racing deletion"),0,0)),()->new TransactionTemplate(manager).execute(tx->{accounts.erase(author.id());return "erased";}));assertThat(db.queryForObject("SELECT count(*) FROM growth_resources WHERE author_id=?",Integer.class,author.id())).isZero();assertThat(db.queryForObject("SELECT status FROM users WHERE id=?",String.class,author.id())).isEqualTo("DELETED");}
}
