package com.greenocean.backend;
import com.greenocean.backend.listener.live.*;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import com.greenocean.backend.account.repository.AccountRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import tools.jackson.databind.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties="greenocean.live-support.enabled=true") @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseILiveSupportIntegrationTest {
 @Autowired MockMvc mvc;@Autowired JdbcTemplate db;@Autowired ObjectMapper json;@Autowired UserRepository users;@Autowired TokenService tokens;@Autowired LiveSupportService live;@Autowired AccountRepository accounts;
 PhaseIFixtures.Actor member,listener,staff;UUID application;
 @BeforeEach void fixture(){PhaseIFixtures.region(db);member=PhaseIFixtures.member(db,users,tokens);listener=PhaseIFixtures.member(db,users,tokens);staff=PhaseIFixtures.member(db,users,tokens);application=PhaseIFixtures.review(db,listener.id(),staff.id(),true);live.setAvailability(listener.id(),listener.session(),0,true,null);}
 String auth(PhaseIFixtures.Actor a){return "Bearer "+a.token();}
 JsonNode getJson(String path,PhaseIFixtures.Actor a)throws Exception{return json.readTree(mvc.perform(get(path).header("Authorization",auth(a))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());}
 JsonNode postJson(String path,PhaseIFixtures.Actor a,Object body)throws Exception{return json.readTree(mvc.perform(post(path).header("Authorization",auth(a)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body))).andExpect(status().is2xxSuccessful()).andReturn().getResponse().getContentAsString());}
 UUID request()throws Exception{return UUID.fromString(postJson("/api/v1/listener-support/requests",member,Map.of("clientRequestId",UUID.randomUUID(),"listenerId",listener.id())).get("id").asText());}
 JsonNode act(UUID id,String action,PhaseIFixtures.Actor who)throws Exception{return postJson("/api/v1/listener-support/requests/"+id+"/"+action,who,Map.of());}
 UUID active()throws Exception{UUID id=request();act(id,"accept",listener);act(id,"activate",member);return id;}
 UUID conversation(UUID id){return db.queryForObject("SELECT conversation_id FROM live_support_requests WHERE id=?",UUID.class,id);}
 void send(UUID conversation,PhaseIFixtures.Actor who,String body,UUID key,int expected)throws Exception{mvc.perform(post("/api/v1/conversations/"+conversation+"/messages").header("Authorization",auth(who)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("body",body,"clientMessageId",key)))).andExpect(status().is(expected));}
 @Test @Tag("happy") void realAcceptanceActivationMessagingEndAndHistoryAreSeparateAndRetrySafe()throws Exception{
  UUID id=request();assertThat(conversation(id)).isNull();act(id,"accept",listener);assertThat(conversation(id)).isNull();
  mvc.perform(post("/api/v1/listener-support/requests/"+id+"/activate").header("Authorization",auth(listener))).andExpect(status().isForbidden());
  var activated=act(id,"activate",member);UUID chat=conversation(id);assertThat(chat).isNotNull();assertThat(act(id,"activate",member).get("conversationId").asText()).isEqualTo(chat.toString());assertThat(activated.get("status").asText()).isEqualTo("ACTIVE");
  UUID key=UUID.randomUUID();send(chat,member,"سلام 🌊",key,201);send(chat,member,"سلام 🌊",key,201);send(chat,member,"Changed retry",key,409);send(chat,listener,"Supportive synthetic reply",UUID.randomUUID(),201);
  assertThat(db.queryForObject("SELECT count(*) FROM messages WHERE conversation_id=? AND message_kind='USER'",Integer.class,chat)).isEqualTo(2);
  postJson("/api/v1/listener-support/sessions/"+id+"/end",member,Map.of());postJson("/api/v1/listener-support/sessions/"+id+"/end",listener,Map.of());send(chat,member,"Cannot reopen",UUID.randomUUID(),409);
  assertThat(getJson("/api/v1/conversations/"+chat+"/messages",member).toString()).contains("سلام");assertThat(db.queryForObject("SELECT ready FROM listener_presence WHERE listener_id=?",Boolean.class,listener.id())).isFalse();
 }
 @Test @Tag("malicious") void callerCannotForgeAcceptanceOrInspectAnotherRequest()throws Exception{
  UUID id=request();var stranger=PhaseIFixtures.member(db,users,tokens);
  mvc.perform(post("/api/v1/listener-support/requests/"+id+"/accept").header("Authorization",auth(member))).andExpect(status().isForbidden());
  mvc.perform(get("/api/v1/listener-support/requests/"+id).header("Authorization",auth(stranger))).andExpect(status().isNotFound());
  mvc.perform(post("/api/v1/listener-support/requests/"+id+"/activate").header("Authorization",auth(member))).andExpect(status().isConflict());assertThat(conversation(id)).isNull();
 }
 @Test @Tag("confused") void noOfflineFallbackOrLegacyInstantMatchExists()throws Exception{
  live.setAvailability(listener.id(),listener.session(),0,false,live.availability(listener.id(),listener.session()).leaseId());
  mvc.perform(post("/api/v1/listener-support/requests").header("Authorization",auth(member)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("clientRequestId",UUID.randomUUID())))).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("NO_AVAILABLE_LISTENER"));
  mvc.perform(post("/api/v1/listeners/match").header("Authorization",auth(member))).andExpect(status().isGone());
  assertThat(db.queryForObject("SELECT count(*) FROM live_support_requests WHERE member_id=?",Integer.class,member.id())).isZero();
 }
 @Test @Tag("malicious") void exactExpiredDeadlineCannotBeAcceptedWithoutScheduler()throws Exception{
  UUID id=request();db.update("UPDATE live_support_requests SET expires_at=clock_timestamp() WHERE id=?",id);
  mvc.perform(post("/api/v1/listener-support/requests/"+id+"/accept").header("Authorization",auth(listener))).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("REQUEST_EXPIRED"));
  assertThat(db.queryForObject("SELECT status FROM live_support_requests WHERE id=?",String.class,id)).isEqualTo("EXPIRED");assertThat(conversation(id)).isNull();
 }
 @Test @Tag("confused") void acceptedOfferHasBoundedActivationAndDoesNotLeaveCapacityOccupied()throws Exception{
  UUID id=request();act(id,"accept",listener);db.update("UPDATE live_support_requests SET activation_expires_at=clock_timestamp() WHERE id=?",id);
  mvc.perform(post("/api/v1/listener-support/requests/"+id+"/activate").header("Authorization",auth(member))).andExpect(status().isConflict());assertThat(conversation(id)).isNull();assertThat(request()).isNotEqualTo(id);
 }
 @Test @Tag("confused") void cancelDeclineAndListenerUnavailabilityAreTerminalWithoutChats()throws Exception{
  UUID id=request();act(id,"cancel",member);act(id,"cancel",member);
  mvc.perform(post("/api/v1/listener-support/requests/"+id+"/accept").header("Authorization",auth(listener))).andExpect(status().isConflict());
  UUID next=request();act(next,"decline",listener);act(next,"decline",listener);assertThat(conversation(next)).isNull();
  UUID third=request();live.setAvailability(listener.id(),listener.session(),0,false,live.availability(listener.id(),listener.session()).leaseId());assertThat(getJson("/api/v1/listener-support/requests/"+third,member).get("status").asText()).isEqualTo("DECLINED");
 }
 @Test @Tag("creative") void rotatingReadinessLeaseRejectsOldDeviceAndRevokedAuthentication()throws Exception{
  var old=live.availability(listener.id(),listener.session());var second=PhaseIFixtures.session(db,users,tokens,listener.id());var current=live.setAvailability(listener.id(),second.session(),0,true,null);
  assertThatThrownBy(()->live.heartbeat(listener.id(),listener.session(),0,old.leaseId())).isInstanceOf(LiveSupportException.class);
  assertThatThrownBy(()->live.setAvailability(listener.id(),listener.session(),0,false,old.leaseId())).isInstanceOf(LiveSupportException.class);
  assertThat(live.availability(listener.id(),second.session()).leaseId()).isEqualTo(current.leaseId());
  db.update("UPDATE user_sessions SET revoked_at=clock_timestamp() WHERE id=?",second.session());
  mvc.perform(post("/api/v1/listeners/me/heartbeat").header("Authorization",auth(second)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("leaseId",current.leaseId())))).andExpect(status().isUnauthorized());
  assertThat(db.queryForObject("SELECT count(*) FROM listener_presence WHERE listener_id=?",Integer.class,listener.id())).isZero();
 }
 @Test @Tag("malicious") void selfCompletedPracticeIsNotIndependentLiveAuthorization()throws Exception{
  db.update("DELETE FROM listener_live_authorizations WHERE application_id=?",application);
  assertThat(db.queryForObject("SELECT live_listener_qualified(?)",Boolean.class,listener.id())).isFalse();
  assertThatThrownBy(()->live.setAvailability(listener.id(),listener.session(),0,true,null)).isInstanceOf(LiveSupportException.class);
  assertThat(db.queryForObject("SELECT status FROM listener_applications WHERE id=?",String.class,application)).isEqualTo("APPROVED");
  mvc.perform(put("/api/v1/admin/listener-live-authorizations/"+application).header("Authorization",auth(member)).contentType(MediaType.APPLICATION_JSON).content("{\"practiceApproved\":true,\"conductVersion\":1}")).andExpect(status().isForbidden());
 }
 @Test @Tag("malicious") void blockImmediatelyClosesLiveSessionAndStopsPollingAndSending()throws Exception{
  UUID id=active(),chat=conversation(id);send(chat,listener,"Private peer response",UUID.randomUUID(),201);
  mvc.perform(put("/api/v1/social/blocks/"+listener.id()).header("Authorization",auth(member))).andExpect(status().isNoContent());
  assertThat(db.queryForObject("SELECT status FROM live_support_requests WHERE id=?",String.class,id)).isEqualTo("ENDED_SAFETY");
  mvc.perform(get("/api/v1/conversations/"+chat+"/messages").header("Authorization",auth(member))).andExpect(status().isForbidden());
  send(chat,listener,"Blocked bypass",UUID.randomUUID(),403);assertThat(getJson("/api/v1/conversations",member).toString()).doesNotContain(chat.toString());
 }
 @Test @Tag("malicious") void suspensionAndListenerRevocationInvalidateActiveCapacity()throws Exception{
  UUID id=active();db.update("UPDATE listener_profiles SET status='SUSPENDED' WHERE user_id=?",listener.id());
  assertThat(db.queryForObject("SELECT status FROM live_support_requests WHERE id=?",String.class,id)).isEqualTo("ENDED_SAFETY");
  db.update("UPDATE listener_profiles SET status='ACTIVE' WHERE user_id=?",listener.id());live.setAvailability(listener.id(),listener.session(),0,true,null);
  UUID next=active();db.update("UPDATE users SET status='SUSPENDED' WHERE id=?",member.id());assertThat(db.queryForObject("SELECT status FROM live_support_requests WHERE id=?",String.class,next)).isEqualTo("ENDED_SAFETY");
 }
 @Test @Tag("creative") void interruptionResumesOnlyWithRecentParticipantsAndEndsAfterBoundedTimeout()throws Exception{
  UUID id=active(),chat=conversation(id);db.update("UPDATE live_support_requests SET member_seen_at=clock_timestamp()-INTERVAL '50 seconds' WHERE id=?",id);
  assertThat(getJson("/api/v1/listener-support/requests/"+id,member).get("status").asText()).isEqualTo("INTERRUPTED");send(chat,member,"No disconnected sends",UUID.randomUUID(),409);
  assertThat(act(id,"heartbeat",member).get("status").asText()).isEqualTo("ACTIVE");
  db.update("UPDATE live_support_requests SET member_seen_at=clock_timestamp()-INTERVAL '200 seconds' WHERE id=?",id);
  assertThat(getJson("/api/v1/listener-support/requests/"+id,member).get("status").asText()).isEqualTo("COMPLETED");mvc.perform(post("/api/v1/listener-support/requests/"+id+"/heartbeat").header("Authorization",auth(member))).andExpect(status().isConflict());
 }
 @Test @Tag("malicious") void ordinaryDirectConversationCannotMasqueradeAsAcceptedListenerSession()throws Exception{
  db.update("UPDATE user_preferences SET allow_message_requests=TRUE WHERE user_id=?",listener.id());
  UUID direct=UUID.fromString(postJson("/api/v1/conversations/users/"+listener.id(),member,Map.of()).get("id").asText());UUID id=active();assertThat(conversation(id)).isNotEqualTo(direct);
  assertThat(db.queryForObject("SELECT service_kind FROM conversations WHERE id=?",String.class,direct)).isEqualTo("COMMUNITY");
  mvc.perform(get("/api/v1/listener-support/conversations/"+direct).header("Authorization",auth(member))).andExpect(status().isNotFound());
 }
 @Test @Tag("malicious") void requestsAndMessagesAreBoundedAndDoNotCollectTopics()throws Exception{
  mvc.perform(post("/api/v1/listener-support/requests").header("Authorization",auth(member)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("clientRequestId",UUID.randomUUID(),"topic","unapproved sensitive choice")))).andExpect(status().isBadRequest());
  UUID id=active(),chat=conversation(id);
  mvc.perform(post("/api/v1/conversations/"+chat+"/messages").header("Authorization",auth(member)).contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"missing retry key\"}")).andExpect(status().isBadRequest());
  send(chat,member,"x".repeat(5001),UUID.randomUUID(),400);
 }
 @Test @Tag("malicious") void rawMessageAndParticipantConstraintsRejectForgedThirdParty()throws Exception{
  UUID id=active(),chat=conversation(id);var third=PhaseIFixtures.member(db,users,tokens);
  db.execute((ConnectionCallback<Void>)connection->{var savepoint=connection.setSavepoint();try(var statement=connection.prepareStatement("INSERT INTO messages(conversation_id,sender_id,message_kind,body) VALUES (?,?,'USER','forged')")){statement.setObject(1,chat);statement.setObject(2,third.id());assertThatThrownBy(statement::executeUpdate).isInstanceOf(java.sql.SQLException.class);}finally{connection.rollback(savepoint);}return null;});
  assertThat(db.queryForObject("SELECT count(*) FROM messages WHERE conversation_id=? AND message_kind='USER'",Integer.class,chat)).isZero();
 }
 @Test @Tag("happy") void exportOwnMetadataAndMessagesDoesNotCopyPeersTranscriptAndErasureReleasesCapacity()throws Exception{
  UUID id=active(),chat=conversation(id);send(chat,member,"Own export marker",UUID.randomUUID(),201);send(chat,listener,"Other private transcript marker",UUID.randomUUID(),201);
  String archive=getJson("/api/v1/account/archive",member).toString();assertThat(archive).contains(id.toString(),"Own export marker").doesNotContain("Other private transcript marker");
  accounts.erase(member.id());assertThat(db.queryForObject("SELECT count(*) FROM live_support_requests WHERE member_id=?",Integer.class,member.id())).isZero();assertThat(db.queryForObject("SELECT is_active FROM conversations WHERE id=?",Boolean.class,chat)).isFalse();
 }
 @Test @Tag("malicious") void pendingIneligibleOrUnsupportedAccountsCannotRequestSupport()throws Exception{
  for(String value:List.of("PENDING","INELIGIBLE","REGION")){
   var who=PhaseIFixtures.member(db,users,tokens);
   if(value.equals("PENDING"))db.update("UPDATE users SET status='PENDING' WHERE id=?",who.id());
   else if(value.equals("INELIGIBLE"))db.update("UPDATE account_eligibility SET age_result='INELIGIBLE' WHERE user_id=?",who.id());
   else db.update("UPDATE account_eligibility SET current_country='DE' WHERE user_id=?",who.id());
   mvc.perform(post("/api/v1/listener-support/requests").header("Authorization",auth(who)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("clientRequestId",UUID.randomUUID())))).andExpect(status().is4xxClientError());
   assertThat(db.queryForObject("SELECT count(*) FROM live_support_requests WHERE member_id=?",Integer.class,who.id())).isZero();
  }
 }
 @Test @Tag("creative") void duplicateRequestIdentifierReturnsSameOfferAndOnlyOneOpenRequestIsAllowed()throws Exception{
  UUID key=UUID.randomUUID();var first=postJson("/api/v1/listener-support/requests",member,Map.of("clientRequestId",key));var again=postJson("/api/v1/listener-support/requests",member,Map.of("clientRequestId",key));assertThat(again.get("id")).isEqualTo(first.get("id"));
  mvc.perform(post("/api/v1/listener-support/requests").header("Authorization",auth(member)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("clientRequestId",UUID.randomUUID())))).andExpect(status().isConflict());
 }
 @Test @Tag("malicious") void rateLimitBoundsRepeatedExplicitRetriesWithoutQueue()throws Exception{
  for(int n=0;n<5;n++){UUID id=request();act(id,"cancel",member);}
  mvc.perform(post("/api/v1/listener-support/requests").header("Authorization",auth(member)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("clientRequestId",UUID.randomUUID())))).andExpect(status().isTooManyRequests());
  assertThat(db.queryForObject("SELECT count(*) FROM live_support_requests WHERE member_id=? AND status='REQUESTED'",Integer.class,member.id())).isZero();
 }
}
