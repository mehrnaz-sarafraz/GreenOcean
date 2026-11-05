package com.greenocean.backend;
import com.greenocean.backend.catalog.service.GrowthLibrary;
import com.greenocean.backend.wellbeing.service.*;
import com.greenocean.backend.wellbeing.repository.WellbeingRepository;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import com.greenocean.backend.account.repository.AccountRepository;
import com.greenocean.backend.account.service.AccountService;
import com.greenocean.backend.common.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.*;
import java.time.*;import java.util.*;
import static org.assertj.core.api.Assertions.*;
abstract class PhaseO3Fixtures {
 @Autowired JdbcTemplate db;@Autowired GrowthLibrary library;@Autowired LearningCourses learning;@Autowired ReviewedQuestions questions;@Autowired PrivateWellbeing privateData;@Autowired UserRepository users;@Autowired TokenService tokens;@Autowired WellbeingRepository wellbeing;@Autowired AccountRepository erasure;@Autowired AccountService archives;@Autowired org.springframework.test.web.servlet.MockMvc mvc;
 PhaseIFixtures.Actor author,reviewer,issuer,learner,other;
 @BeforeEach void setup(){PhaseIFixtures.region(db);author=PhaseIFixtures.member(db,users,tokens);reviewer=PhaseIFixtures.member(db,users,tokens);issuer=PhaseIFixtures.member(db,users,tokens);learner=PhaseIFixtures.member(db,users,tokens);other=PhaseIFixtures.member(db,users,tokens);role(issuer.id(),"ADMIN");role(reviewer.id(),"MODERATOR");for(String action:List.of("DRAFT","SUBMIT"))grant(author.id(),action);for(String action:List.of("REVIEW","APPROVE","PUBLISH","RETIRE"))grant(reviewer.id(),action);}
 void role(UUID who,String name){db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name=? ON CONFLICT DO NOTHING",who,name);}
 void grant(UUID who,String action){library.grant(issuer.id(),new GrowthLibrary.Grant(who,action,"GENERAL","Synthetic isolated curriculum permission",Instant.now().plusSeconds(3600)));}
 Map<String,Object> lesson(String title,boolean quiz){var row=new LinkedHashMap<String,Object>();row.put("title",title);row.put("body","Choose one manageable action that matters to you. Consider the time and resources you have. You may adapt or skip this practice freely; a missed day does not make the goal a failure.");row.put("objectives","Distinguish a manageable plan from pressure.");row.put("minutes",5);row.put("required",true);if(quiz)row.put("exercise",Map.of("type","CHOICE","instruction","Which option preserves choice?","choices",List.of("Choose one manageable action","Punish a missed day"),"correctIndex",0,"explanation","A manageable action respects autonomy; missed days have no penalty."));return row;}
 Map<String,Object> doc(){var d=new LinkedHashMap<String,Object>();d.put("kind","COURSE");d.put("title","Synthetic manageable planning");d.put("summary","Original general educational material, not treatment.");d.put("language","en");d.put("scope","GENERAL");d.put("topic","Planning");d.put("why","General education about adapting small steps");d.put("audience","ADULT");d.put("rights","Original synthetic QA curriculum");d.put("sources",List.of("https://example.org/educational-source"));d.put("modules",List.of(Map.of("title","Small steps","lessons",List.of(lesson("Choose a step",true),lesson("Adapt without pressure",false)))));return d;}
 UUID create(){return(UUID)library.create(author.id(),new GrowthLibrary.Draft(doc(),0,0)).get("resource_id");}
 GrowthLibrary.Decision decision(UUID id,String action){var r=library.editorial(author.id(),id);return new GrowthLibrary.Decision(((Number)r.get("revision")).intValue(),((Number)r.get("etag")).longValue(),action,"Synthetic independent curriculum source/rights review",true,true);}
 void publish(UUID id){library.transition(author.id(),id,decision(id,"SUBMIT"));library.transition(reviewer.id(),id,decision(id,"APPROVE"));library.transition(reviewer.id(),id,decision(id,"PUBLISH"));}
 UUID course(){UUID id=create();publish(id);return id;}
 List<UUID> keys(UUID id){int revision=((Number)library.editorial(author.id(),id).get("revision")).intValue();return db.queryForList("SELECT lesson_key FROM learning_lessons(?,?) ORDER BY module_position,lesson_position",UUID.class,id,revision);}
 Map<String,Object> open(UUID id,UUID key){return learning.open(learner.id(),id,key);}
 Map<String,Object> complete(UUID id,UUID key){var l=open(id,key);return learning.complete(learner.id(),id,key,new LearningCourses.Completion((String)l.get("content_digest"),true));}
 Map<String,Object> forkDoc(UUID id){return new LinkedHashMap<>((Map<String,Object>)library.editorial(author.id(),id).get("document"));}
 void fork(UUID id,Map<String,Object> doc){var row=library.editorial(author.id(),id);library.edit(author.id(),id,new GrowthLibrary.Draft(doc,((Number)row.get("revision")).intValue(),((Number)row.get("etag")).longValue()),true);publish(id);}
}
