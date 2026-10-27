package com.greenocean.backend;
import com.greenocean.backend.listener.live.*;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.*;
import com.greenocean.backend.identity.AccessPolicy;
import com.greenocean.backend.messaging.repository.MessagingRepository;
import com.greenocean.backend.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest(properties="greenocean.live-support.enabled=true") @ActiveProfiles("test")
@org.springframework.test.annotation.DirtiesContext(classMode=org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class PhaseIConcurrencyTest extends MigrationTestSupport {
 static final String SCHEMA="qa_i_race_"+UUID.randomUUID().toString().replace("-","");
 @org.springframework.test.context.DynamicPropertySource static void isolatedSchema(org.springframework.test.context.DynamicPropertyRegistry properties){
  String url=System.getenv("GREENOCEAN_TEST_DB_URL");
  if(url==null||url.isBlank())throw new IllegalStateException("An isolated test database is required");
  properties.add("spring.datasource.url",()->url+(url.contains("?")?"&":"?")+"currentSchema="+SCHEMA+",public");
  properties.add("spring.flyway.schemas",()->SCHEMA);properties.add("spring.flyway.default-schema",()->SCHEMA);
  properties.add("spring.jpa.properties.hibernate.default_schema",()->SCHEMA);
 }
 @AfterAll static void removeSchema(@Autowired JdbcTemplate database){database.execute("DROP SCHEMA "+SCHEMA+" CASCADE");}
 @Autowired JdbcTemplate db;@Autowired UserRepository users;@Autowired TokenService tokens;@Autowired LiveSupportService live;
 @Autowired PlatformTransactionManager manager;@Autowired AccessPolicy access;@Autowired LiveSupportSettings settings;@Autowired AuthRateLimit rates;@Autowired MessagingRepository messaging;@Autowired AccountRepository accounts;
 PhaseIFixtures.Actor member,other,listener,staff;List<UUID> actors;LiveSupportService second;
 @BeforeEach void setup(){PhaseIFixtures.region(db);staff=PhaseIFixtures.member(db,users,tokens);listener=PhaseIFixtures.member(db,users,tokens);member=PhaseIFixtures.member(db,users,tokens);other=PhaseIFixtures.member(db,users,tokens);actors=List.of(member.id(),other.id(),listener.id(),staff.id());PhaseIFixtures.review(db,listener.id(),staff.id(),true);live.setAvailability(listener.id(),listener.session(),0,true,null);second=new LiveSupportService(db,access,settings,rates,messaging);}
 <T>T tx(Supplier<T> operation){return new TransactionTemplate(manager).execute(status->operation.get());}
 Object outcome(Supplier<?> operation){try{return tx(operation);}catch(LiveSupportException denied){return denied.code();}}
 List<Object> race(Supplier<?> first,Supplier<?> last)throws Exception{
  try(var pool=Executors.newFixedThreadPool(2)){var start=new CountDownLatch(1);var a=pool.submit(()->{start.await();return outcome(first);});var b=pool.submit(()->{start.await();return outcome(last);});start.countDown();return List.of(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS));}
 }
 LiveSupportView request(){return live.create(member.id(),UUID.randomUUID(),listener.id());}
 LiveSupportView accepted(){var r=request();return live.accept(r.id(),listener.id());}
 @AfterEach void clean(){tx(()->{
  var chats=db.queryForList("SELECT conversation_id FROM live_support_requests WHERE member_id IN (?,?) AND conversation_id IS NOT NULL",UUID.class,member.id(),other.id());
  db.update("DELETE FROM live_support_requests WHERE member_id IN (?,?) OR listener_id=?",member.id(),other.id(),listener.id());
  for(UUID chat:chats)db.update("DELETE FROM conversations WHERE id=?",chat);
  db.update("DELETE FROM listener_live_authorizations WHERE listener_id=?",listener.id());db.update("DELETE FROM listener_applications WHERE user_id=?",listener.id());
  for(UUID id:actors){db.update("DELETE FROM audit_logs WHERE actor_user_id=?",id);db.update("DELETE FROM account_deletions WHERE user_id=?",id);db.update("DELETE FROM users WHERE id=?",id);}return true;
 });}
 @Test @Tag("malicious") void twoServiceInstancesCompetingForOneListenerHaveOneCapacityWinner()throws Exception{
  var outcomes=race(()->live.create(member.id(),UUID.randomUUID(),listener.id()),()->second.create(other.id(),UUID.randomUUID(),listener.id()));
  assertThat(outcomes.stream().filter(v->v instanceof LiveSupportView).count()).isEqualTo(1);
  assertThat(db.queryForObject("SELECT count(*) FROM live_support_requests WHERE listener_id=? AND status='REQUESTED'",Integer.class,listener.id())).isEqualTo(1);
 }
 @Test @Tag("creative") void memberDuplicateDevicesAndListenerDuplicateAcceptanceConvergeToOneRequest()throws Exception{
  UUID key=UUID.randomUUID();var result=race(()->live.create(member.id(),key,listener.id()),()->second.create(member.id(),key,listener.id()));
  assertThat(result).allMatch(value->value instanceof LiveSupportView);UUID id=((LiveSupportView)result.getFirst()).id();assertThat(((LiveSupportView)result.getLast()).id()).isEqualTo(id);
  var accept=race(()->live.accept(id,listener.id()),()->second.accept(id,listener.id()));assertThat(accept).allMatch(v->v instanceof LiveSupportView r&&r.status().equals("ACCEPTED"));
  assertThat(db.queryForObject("SELECT conversation_id FROM live_support_requests WHERE id=?",UUID.class,id)).isNull();
 }
 @Test @Tag("malicious") void acceptanceVersusCancellationAlwaysEndsCancelledWithoutConversation()throws Exception{
  var r=request();race(()->live.accept(r.id(),listener.id()),()->second.cancel(r.id(),member.id()));
  assertThat(live.get(r.id(),member.id()).status()).isEqualTo("CANCELLED");assertThat(live.get(r.id(),member.id()).conversationId()).isNull();
 }
 @Test @Tag("malicious") void exactExpirationWinsAgainstConcurrentAcceptanceAndRestartedWorker()throws Exception{
  var r=request();db.update("UPDATE live_support_requests SET expires_at=clock_timestamp() WHERE id=?",r.id());
  race(()->live.accept(r.id(),listener.id()),()->{second.reconcile();return true;});assertThat(live.get(r.id(),member.id()).status()).isEqualTo("EXPIRED");
 }
 @Test @Tag("malicious") void blockingVersusAcceptanceAndSuspensionVersusActivationCannotLeaveLiveAccess()throws Exception{
  var r=request();race(()->live.accept(r.id(),listener.id()),()->{db.update("INSERT INTO blocks(blocker_id,blocked_id) VALUES (?,?)",member.id(),listener.id());return true;});
  assertThat(live.get(r.id(),member.id()).status()).isEqualTo("ENDED_SAFETY");
  db.update("DELETE FROM blocks WHERE blocker_id=? AND blocked_id=?",member.id(),listener.id());live.setAvailability(listener.id(),listener.session(),0,true,null);
  var next=accepted();race(()->live.activate(next.id(),member.id()),()->{db.update("UPDATE users SET status='SUSPENDED' WHERE id=?",listener.id());return true;});
  assertThat(live.get(next.id(),member.id()).status()).isEqualTo("ENDED_SAFETY");
 }
 @Test @Tag("creative") void restartedServiceRestoresAcceptedStateAndDuplicateActivationAndEndAreIdempotent()throws Exception{
  var r=accepted();assertThat(tx(()->second.get(r.id(),member.id())).status()).isEqualTo("ACCEPTED");
  var activated=race(()->live.activate(r.id(),member.id()),()->second.activate(r.id(),member.id()));
  assertThat(activated).allMatch(v->v instanceof LiveSupportView);assertThat(((LiveSupportView)activated.getFirst()).conversationId()).isEqualTo(((LiveSupportView)activated.getLast()).conversationId());
  race(()->live.end(r.id(),member.id()),()->second.end(r.id(),listener.id()));assertThat(live.get(r.id(),member.id()).status()).isEqualTo("COMPLETED");
 }
 @Test @Tag("confused") void databaseFailureRollsBackConversationAndAcceptanceReservationRemainsRecoverable(){
  var r=accepted();int before=db.queryForObject("SELECT count(*) FROM conversations WHERE service_kind='LISTENER'",Integer.class);
  assertThatThrownBy(()->tx(()->{live.activate(r.id(),member.id());throw new IllegalStateException("Synthetic commit failure");})).isInstanceOf(IllegalStateException.class);
  assertThat(live.get(r.id(),member.id()).status()).isEqualTo("ACCEPTED");assertThat(db.queryForObject("SELECT count(*) FROM conversations WHERE service_kind='LISTENER'",Integer.class)).isEqualTo(before);
  assertThat(live.activate(r.id(),member.id()).conversationId()).isNotNull();
 }
 @Test @Tag("malicious") void logoutVersusHeartbeatAndAuthorizationRevocationVersusActiveSessionFailClosed()throws Exception{
  var lease=live.availability(listener.id(),listener.session());race(()->live.heartbeat(listener.id(),listener.session(),0,lease.leaseId()),()->{db.update("UPDATE user_sessions SET revoked_at=clock_timestamp() WHERE id=?",listener.session());return true;});
  assertThat(db.queryForObject("SELECT count(*) FROM listener_presence WHERE listener_id=?",Integer.class,listener.id())).isZero();
  var replacement=PhaseIFixtures.session(db,users,tokens,listener.id());live.setAvailability(listener.id(),replacement.session(),0,true,null);var r=accepted();live.activate(r.id(),member.id());
  db.update("UPDATE listener_live_authorizations SET revoked_at=clock_timestamp() WHERE listener_id=?",listener.id());assertThat(live.get(r.id(),member.id()).status()).isEqualTo("ENDED_SAFETY");
 }
 @Test @Tag("malicious") void requesterErasureDuringActiveSessionReleasesSlotAndDoesNotPreservePrivateRequest(){
  var r=accepted();live.activate(r.id(),member.id());tx(()->{accounts.erase(member.id());return true;});
  assertThat(db.queryForObject("SELECT count(*) FROM live_support_requests WHERE member_id=?",Integer.class,member.id())).isZero();
  assertThat(db.queryForObject("SELECT status FROM users WHERE id=?",String.class,member.id())).isEqualTo("DELETED");
 }
 @Test @Tag("creative") void committedAcceptanceRetryAndRestartedPendingReadNeverDuplicateOffers(){
  var pending=request();assertThat(tx(()->second.get(pending.id(),member.id())).status()).isEqualTo("REQUESTED");
  var committed=live.accept(pending.id(),listener.id());var retry=tx(()->second.accept(pending.id(),listener.id()));
  assertThat(retry.id()).isEqualTo(committed.id());assertThat(retry.activationExpiresAt()).isEqualTo(committed.activationExpiresAt());
 }

 @Test @Tag("malicious") void distinctDuplicateRequestsAndRequesterDeletionVersusActivationHaveOneSafeOutcome()throws Exception{
  var results=race(()->live.create(member.id(),UUID.randomUUID(),listener.id()),()->second.create(member.id(),UUID.randomUUID(),listener.id()));
  assertThat(results.stream().filter(v->v instanceof LiveSupportView).count()).isEqualTo(1);
  var pending=(LiveSupportView)results.stream().filter(v->v instanceof LiveSupportView).findFirst().orElseThrow();live.accept(pending.id(),listener.id());
  race(()->live.activate(pending.id(),member.id()),()->{accounts.erase(member.id());return true;});
  assertThat(db.queryForObject("SELECT count(*) FROM live_support_requests WHERE member_id=?",Integer.class,member.id())).isZero();
  assertThat(db.queryForObject("SELECT count(*) FROM conversations c JOIN conversation_members m ON m.conversation_id=c.id WHERE m.user_id=? AND c.is_active",Integer.class,member.id())).isZero();
 }
 @Test @Tag("confused") void failedRequestCreationTransactionDoesNotLeakCapacity(){
  assertThatThrownBy(()->tx(()->{live.create(member.id(),UUID.randomUUID(),listener.id());throw new IllegalStateException("Synthetic request failure");})).isInstanceOf(IllegalStateException.class);
  assertThat(db.queryForObject("SELECT count(*) FROM live_support_requests WHERE member_id=?",Integer.class,member.id())).isZero();
  assertThat(request().status()).isEqualTo("REQUESTED");
 }

}
