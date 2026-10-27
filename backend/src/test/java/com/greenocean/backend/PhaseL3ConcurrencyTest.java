package com.greenocean.backend;
import com.greenocean.backend.moderation.service.SocialReviewService;
import com.greenocean.backend.common.exception.ConflictException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest @ActiveProfiles("test")
class PhaseL3ConcurrencyTest {
 @Autowired JdbcTemplate db;@Autowired SocialReviewService reviews;@Autowired PlatformTransactionManager manager;
 List<UUID> users;UUID owner,first,second,post,assessment;
 @BeforeEach void fixtures(){users=new ArrayList<>();for(int i=0;i<3;i++){UUID id=UUID.randomUUID();users.add(id);db.update("INSERT INTO users(id,email,password_hash,status) VALUES(?,?,'synthetic-only','ACTIVE')",id,"l3-race-"+id+"@example.test");if(i>0)db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='MODERATOR'",id);}owner=users.get(0);first=users.get(1);second=users.get(2);post=UUID.randomUUID();assessment=UUID.randomUUID();db.update("INSERT INTO posts(id,author_id,body,is_anonymous,status) VALUES(?,?,'Synthetic race context',TRUE,'UNDER_REVIEW')",post,owner);db.update("INSERT INTO social_assessments(id,post_id,revision,policy_version,outcome,reason_code) VALUES(?,?,1,'synthetic','HOLD_FOR_REVIEW','CONTEXT_REVIEW')",assessment,post);}
 @AfterEach void cleanup(){db.update("DELETE FROM moderation_appeals WHERE assessment_id=?",assessment);db.update("DELETE FROM audit_logs WHERE entity_id=?",assessment);db.update("DELETE FROM social_assessments WHERE id=?",assessment);db.update("DELETE FROM posts WHERE id=?",post);for(UUID user:users){db.update("DELETE FROM user_roles WHERE user_id=?",user);db.update("DELETE FROM users WHERE id=?",user);}}
 @Test void conflictingModeratorsHaveOneCommittedWinner()throws Exception{
  var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);try{
   var tasks=new ArrayList<Future<String>>();for(int i=0;i<2;i++){final int n=i;tasks.add(pool.submit(()->{start.await();try{reviews.decide(n==0?first:second,assessment,1,n==0?"ALLOW":"BLOCK_PROHIBITED","Synthetic reviewed context");return "COMMITTED";}catch(ConflictException expected){return "CONFLICT";}}));}start.countDown();
   assertThat(List.of(tasks.get(0).get(30,TimeUnit.SECONDS),tasks.get(1).get(30,TimeUnit.SECONDS))).containsExactlyInAnyOrder("COMMITTED","CONFLICT");
   assertThat(db.queryForObject("SELECT count(*) FROM audit_logs WHERE entity_id=?",Integer.class,assessment)).isEqualTo(1);
  }finally{pool.shutdownNow();}
 }
 @Test void simultaneousAppealsCreateOneRecord()throws Exception{
  var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);try{var tasks=new ArrayList<Future<String>>();for(int i=0;i<2;i++)tasks.add(pool.submit(()->{start.await();try{reviews.appeal(owner,assessment,"Synthetic context appeal");return "CREATED";}catch(ConflictException expected){return "CONFLICT";}}));start.countDown();assertThat(List.of(tasks.get(0).get(30,TimeUnit.SECONDS),tasks.get(1).get(30,TimeUnit.SECONDS))).containsExactlyInAnyOrder("CREATED","CONFLICT");assertThat(db.queryForObject("SELECT count(*) FROM moderation_appeals WHERE assessment_id=?",Integer.class,assessment)).isEqualTo(1);}finally{pool.shutdownNow();}
 }
 @Test void editCommittingDuringReviewInvalidatesApproval()throws Exception{
  var pool=Executors.newFixedThreadPool(2);var locked=new CountDownLatch(1);var release=new CountDownLatch(1);
  try{var edit=pool.submit(()->new TransactionTemplate(manager).execute(s->{db.queryForList("SELECT id FROM posts WHERE id=? FOR UPDATE",post);locked.countDown();try{release.await(5,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new RuntimeException(e);}db.update("UPDATE posts SET body='New unreviewed synthetic revision',revision=2 WHERE id=?",post);return true;}));assertThat(locked.await(5,TimeUnit.SECONDS)).isTrue();var decision=pool.submit(()->{try{reviews.decide(first,assessment,1,"ALLOW","Old revision approval");return "COMMITTED";}catch(ConflictException expected){return "CONFLICT";}});release.countDown();edit.get(30,TimeUnit.SECONDS);assertThat(decision.get(30,TimeUnit.SECONDS)).isEqualTo("CONFLICT");assertThat(db.queryForObject("SELECT status FROM posts WHERE id=?",String.class,post)).isEqualTo("UNDER_REVIEW");}finally{release.countDown();pool.shutdownNow();}
 }
}
