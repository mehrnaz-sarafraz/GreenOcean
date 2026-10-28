package com.greenocean.backend;
import com.greenocean.backend.community.service.GroupProposals;import org.junit.jupiter.api.*;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import org.springframework.test.context.ActiveProfiles;import java.util.*;import java.util.concurrent.*;import static org.assertj.core.api.Assertions.*;
@SpringBootTest(properties={"greenocean.groups.proposals-enabled=true","greenocean.groups.activation-approved=true"}) @AutoConfigureMockMvc @ActiveProfiles("test")
class PhaseM1ConcurrencyTest extends PhaseLFixtures {
 @Autowired GroupProposals proposals;
 @Test @Tag("malicious") void concurrentApprovalsActivateExactlyOneGroup()throws Exception{
  var owner=member();var reviewer=member();db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='MODERATOR'",reviewer.id());
  UUID id=(UUID)proposals.save(owner.id(),null,new GroupProposals.Input("Concurrent circle "+UUID.randomUUID(),"A synthetic peer circle","Shared respectful experiences","HIDDEN",List.of("Respect privacy"),0)).get("id");proposals.transition(owner.id(),id,1,"SUBMIT");
  var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);try{Callable<Boolean> task=()->{start.await();try{proposals.decide(reviewer.id(),id,1,"APPROVED","Independent synthetic review");return true;}catch(com.greenocean.backend.common.exception.ConflictException expected){return false;}};var a=pool.submit(task);var b=pool.submit(task);start.countDown();assertThat(List.of(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS))).containsExactlyInAnyOrder(true,false);assertThat(db.queryForObject("SELECT count(*) FROM community_proposal_reviews WHERE proposal_id=? AND outcome='APPROVED'",Integer.class,id)).isEqualTo(1);assertThat(db.queryForObject("SELECT count(*) FROM communities WHERE created_by=?",Integer.class,owner.id())).isEqualTo(1);}finally{pool.shutdownNow();}
 }
}
