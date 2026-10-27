package com.greenocean.backend;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class PhaseL1ConcurrencyTest extends PhaseLFixtures {
 @Autowired com.greenocean.backend.post.service.PostService posts;
 @Autowired com.greenocean.backend.account.repository.AccountRepository accounts;
 @Test void simultaneousDuplicatePublicationHasOneWinner()throws Exception{
  var a=member();var request=json.readValue(json.writeValueAsString(payload("Concurrent duplicate synthetic story",true)),com.greenocean.backend.post.dto.CreatePostRequest.class);
  var tx=new TransactionTemplate(transactionManager);var pool=Executors.newFixedThreadPool(4);var start=new CountDownLatch(1);
  try{var tasks=new ArrayList<Future<String>>();for(int i=0;i<4;i++)tasks.add(pool.submit(()->{start.await();try{tx.execute(s->posts.create(a.id(),request));return "CREATED";}catch(com.greenocean.backend.common.exception.ConflictException expected){return "CONFLICT";}}));start.countDown();
   var outcomes=new ArrayList<String>();for(var task:tasks)outcomes.add(task.get(30,TimeUnit.SECONDS));assertThat(outcomes).containsExactlyInAnyOrder("CREATED","CONFLICT","CONFLICT","CONFLICT");
   assertThat(db.queryForObject("SELECT COUNT(*) FROM posts WHERE author_id=?",Integer.class,a.id())).isEqualTo(1);
  }finally{pool.shutdownNow();tx.executeWithoutResult(s->accounts.erase(a.id()));}
 }
}
