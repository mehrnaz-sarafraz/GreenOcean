package com.greenocean.backend;
import com.greenocean.backend.community.service.GroupProposals;import org.junit.jupiter.api.*;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import org.springframework.test.context.ActiveProfiles;import java.util.*;import java.util.concurrent.*;import static org.assertj.core.api.Assertions.*;
@SpringBootTest(properties={"greenocean.groups.proposals-enabled=true","greenocean.groups.activation-approved=true"}) @AutoConfigureMockMvc @ActiveProfiles("test")
class PhaseM1ConcurrencyTest extends PhaseLFixtures {
 @Autowired GroupProposals proposals;
 UUID isolatedUser(){UUID id=UUID.randomUUID();db.update("INSERT INTO users(id,email,password_hash,status,email_verified) VALUES(?,?,'synthetic-only','ACTIVE',TRUE)",id,"m-race-"+id+"@example.test");return id;}
 @Test @Tag("malicious") void concurrentApprovalsActivateExactlyOneGroup()throws Exception{
  // Legacy-eligible isolated fixtures: no registration outbox or shared country-catalog mutation.
  UUID owner=isolatedUser(),reviewer=isolatedUser();UUID id=null;var pool=Executors.newFixedThreadPool(2);
  try{db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='MODERATOR'",reviewer);
   id=(UUID)proposals.save(owner,null,new GroupProposals.Input("Concurrent circle "+UUID.randomUUID(),"A synthetic peer circle","Shared respectful experiences","HIDDEN",List.of("Respect privacy"),0)).get("id");proposals.transition(owner,id,1,"SUBMIT");final UUID proposal=id;
   var start=new CountDownLatch(1);Callable<Boolean> task=()->{start.await();try{proposals.decide(reviewer,proposal,1,"APPROVED","Independent synthetic review");return true;}catch(com.greenocean.backend.common.exception.ConflictException expected){return false;}};var a=pool.submit(task);var b=pool.submit(task);start.countDown();assertThat(List.of(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS))).containsExactlyInAnyOrder(true,false);assertThat(db.queryForObject("SELECT count(*) FROM community_proposal_reviews WHERE proposal_id=? AND outcome='APPROVED'",Integer.class,id)).isEqualTo(1);assertThat(db.queryForObject("SELECT count(*) FROM communities WHERE created_by=?",Integer.class,owner)).isEqualTo(1);
  }finally{pool.shutdownNow();pool.awaitTermination(10,TimeUnit.SECONDS);db.update("DELETE FROM notifications WHERE user_id IN(?,?)",owner,reviewer);db.update("DELETE FROM audit_logs WHERE actor_user_id IN(?,?)",owner,reviewer);db.update("DELETE FROM community_proposals WHERE owner_id=?",owner);db.update("DELETE FROM communities WHERE created_by=?",owner);db.update("DELETE FROM user_roles WHERE user_id=?",reviewer);db.update("DELETE FROM users WHERE id IN(?,?)",owner,reviewer);}
 }
}
