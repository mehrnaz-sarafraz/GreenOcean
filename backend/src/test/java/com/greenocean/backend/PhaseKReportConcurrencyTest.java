package com.greenocean.backend;

import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.moderation.dto.CreateReportRequest;
import com.greenocean.backend.moderation.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test")
class PhaseKReportConcurrencyTest {
 @Autowired JdbcTemplate db;
 @Autowired ReportService reports;
 @Test @Tag("malicious") void parallelDuplicateReportsCreateExactlyOneOpenRecord() throws Exception {
  UUID owner=db.queryForObject("INSERT INTO users(email,password_hash,status) VALUES (?,'synthetic','ACTIVE') RETURNING id",UUID.class,"k-report-owner-"+UUID.randomUUID()+"@example.test");
  UUID reporter=db.queryForObject("INSERT INTO users(email,password_hash,status) VALUES (?,'synthetic','ACTIVE') RETURNING id",UUID.class,"k-reporter-"+UUID.randomUUID()+"@example.test");
  UUID post=db.queryForObject("INSERT INTO posts(author_id,body,visibility,status) VALUES (?,'Synthetic concurrency target','PUBLIC','PUBLISHED') RETURNING id",UUID.class,owner);
  try(var pool=Executors.newFixedThreadPool(4)) {
   var start=new CountDownLatch(1);var results=new ArrayList<Future<String>>();
   for(int n=0;n<4;n++)results.add(pool.submit(()->{start.await();try{reports.create(reporter,new CreateReportRequest("POST",post,"OTHER","Synthetic duplicate submission"));return "created";}catch(ConflictException duplicate){return "duplicate";}}));
   start.countDown();var outcomes=new ArrayList<String>();for(var result:results)outcomes.add(result.get(10,TimeUnit.SECONDS));
   assertThat(outcomes).containsExactlyInAnyOrder("created","duplicate","duplicate","duplicate");
   assertThat(db.queryForObject("SELECT count(*) FROM reports WHERE reporter_id=? AND post_id=? AND status IN ('PENDING','IN_REVIEW')",Integer.class,reporter,post)).isEqualTo(1);
  } finally {
   db.update("DELETE FROM reports WHERE reporter_id=?",reporter);db.update("DELETE FROM posts WHERE id=?",post);
   db.update("DELETE FROM users WHERE id IN (?,?)",owner,reporter);
  }
 }
}
