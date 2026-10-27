package com.greenocean.backend;
import org.junit.jupiter.api.*;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import org.springframework.test.context.ActiveProfiles;import org.springframework.transaction.annotation.Transactional;import org.springframework.http.MediaType;import java.util.*;
import static org.assertj.core.api.Assertions.*;import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseL2SocialTest extends PhaseLFixtures {
 @Test @Tag("malicious") void followingCannotRevealAnonymousFollowedAuthors()throws Exception{
  var a=member();var b=member();db.update("INSERT INTO follows(follower_id,following_id) VALUES(?,?)",b.id(),a.id());
  String named=publish(a,"Named followed reflection",false).path("id").asText(),anonymous=publish(a,"Anonymous neutral reflection",true).path("id").asText();
  assertThat(read("/api/v1/posts/discovery?mode=FOLLOWING",b).toString()).contains(named).doesNotContain(anonymous);
  String discover=read("/api/v1/posts/discovery",b).toString();assertThat(discover).contains(named,anonymous);
  String anonymousJson=read("/api/v1/posts/"+anonymous,b).toString();assertThat(anonymousJson).doesNotContain(a.id().toString(),a.email());
  db.update("DELETE FROM follows WHERE follower_id=? AND following_id=?",b.id(),a.id());assertThat(read("/api/v1/posts/discovery?mode=FOLLOWING",b).toString()).doesNotContain(named);
 }
 @Test @Tag("happy") void savedPostsRecheckPermissionAndPermitStaleRemoval()throws Exception{
  var a=member();var b=member();String id=publish(a,"Saved private library story",true).path("id").asText();
  mvc.perform(put("/api/v1/posts/"+id+"/bookmark").header("Authorization","Bearer "+b.token())).andExpect(status().isNoContent());
  String saved=read("/api/v1/posts/discovery?mode=SAVED",b).toString();assertThat(saved).contains(id).doesNotContain(a.id().toString());
  db.update("INSERT INTO blocks(blocker_id,blocked_id) VALUES(?,?)",a.id(),b.id());assertThat(read("/api/v1/posts/discovery?mode=SAVED",b).toString()).doesNotContain(id);
  mvc.perform(delete("/api/v1/posts/"+id+"/bookmark").header("Authorization","Bearer "+b.token())).andExpect(status().isNoContent());
  assertThat(db.queryForObject("SELECT COUNT(*) FROM bookmarks WHERE user_id=?",Integer.class,b.id())).isZero();
 }
 @Test @Tag("creative") void cursorPaginationIsStableUnderANewerInsert()throws Exception{
  var a=member();for(int i=0;i<4;i++)publish(a,"Chronological L2 marker "+i,true);
  var first=read("/api/v1/posts/discovery?size=2",a);String cursor=first.path("nextCursor").asText();String inserted=publish(a,"New chronological L2 marker",true).path("id").asText();
  var next=json.readTree(mvc.perform(get("/api/v1/posts/discovery").param("size","2").param("cursor",cursor).header("Authorization","Bearer "+a.token())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
  assertThat(next.toString()).doesNotContain(inserted);for(var p:first.path("items"))assertThat(next.toString()).doesNotContain(p.path("id").asText());
  assertThat(next.path("items").size()).isEqualTo(2);
 }
 @Test @Tag("malicious") void newMessagesNeedRecipientAcceptanceAndCurrentSettings()throws Exception{
  var a=member();var b=member();var outsider=member();db.update("UPDATE user_preferences SET allow_message_requests=TRUE WHERE user_id=?",a.id());
  var r=submit("/api/v1/conversations/users/"+a.id(),b,Map.of(),201);String id=r.path("id").asText();assertThat(r.path("status").asText()).isEqualTo("PENDING");
  mvc.perform(get("/api/v1/conversations/"+id+"/messages").header("Authorization","Bearer "+b.token())).andExpect(status().isForbidden());
  submit("/api/v1/conversations/"+id+"/messages",b,Map.of("body","Unsolicited body"),403);
  submit("/api/v1/conversations/requests/"+id+"/decision",outsider,Map.of("action","ACCEPT"),404);
  assertThat(read("/api/v1/conversations",a).toString()).doesNotContain(id);assertThat(read("/api/v1/conversations/requests",a).toString()).contains(id);
  db.update("UPDATE user_preferences SET allow_message_requests=FALSE WHERE user_id=?",a.id());submit("/api/v1/conversations/requests/"+id+"/decision",a,Map.of("action","ACCEPT"),403);
  db.update("UPDATE user_preferences SET allow_message_requests=TRUE WHERE user_id=?",a.id());
  mvc.perform(post("/api/v1/conversations/requests/"+id+"/decision").header("Authorization","Bearer "+a.token()).contentType(MediaType.APPLICATION_JSON).content("{\"action\":\"ACCEPT\"}")).andExpect(status().isOk());
  read("/api/v1/conversations/"+id+"/messages",b);
  assertThat(db.queryForObject("SELECT service_kind FROM conversations WHERE id=?",String.class,UUID.fromString(id))).isEqualTo("COMMUNITY");
 }
 @Test void decliningDoesNotOpenAConversationOrAllowFloodingRetries()throws Exception{
  var a=member();var b=member();db.update("UPDATE user_preferences SET allow_message_requests=TRUE WHERE user_id=?",a.id());String id=submit("/api/v1/conversations/users/"+a.id(),b,Map.of(),201).path("id").asText();
  mvc.perform(post("/api/v1/conversations/requests/"+id+"/decision").header("Authorization","Bearer "+a.token()).contentType(MediaType.APPLICATION_JSON).content("{\"action\":\"DECLINE\"}")).andExpect(status().isOk());
  submit("/api/v1/conversations/users/"+a.id(),b,Map.of(),409);
  mvc.perform(get("/api/v1/conversations/"+id+"/messages").header("Authorization","Bearer "+b.token())).andExpect(status().isForbidden());
 }
 @Test void replyParentMustBeVisibleAndSamePostAndOwnerCanDelete()throws Exception{
  var a=member();var b=member();String p=publish(a,"Thread story",true).path("id").asText(),other=publish(a,"Another thread story",true).path("id").asText();
  String c=submit("/api/v1/posts/"+p+"/comments",b,Map.of("body","Supportive response","anonymous",true),201).path("id").asText();
  submit("/api/v1/posts/"+other+"/comments",a,Map.of("body","Forged parent","parentCommentId",c,"anonymous",false),400);
  submit("/api/v1/posts/"+p+"/comments",a,Map.of("body","Thank you for your support","parentCommentId",c,"anonymous",false),201);
  assertThat(read("/api/v1/posts/"+p+"/comments",a).toString()).doesNotContain(b.id().toString());
  mvc.perform(delete("/api/v1/comments/"+c).header("Authorization","Bearer "+a.token())).andExpect(status().isNotFound());
  mvc.perform(delete("/api/v1/comments/"+c).header("Authorization","Bearer "+b.token())).andExpect(status().isNoContent());
  submit("/api/v1/posts/"+p+"/comments",a,Map.of("body","Deleted parent retry","parentCommentId",c,"anonymous",false),400);
 }
 @Test void anonymousReplyActorAndHiddenCommentNoticesAreMinimized()throws Exception{
  var a=member();var b=member();String p=publish(a,"Notification privacy marker",false).path("id").asText();
  String c=submit("/api/v1/posts/"+p+"/comments",b,Map.of("body","Private anonymous reply marker","anonymous",true),201).path("id").asText();
  var notices=read("/api/v1/notifications",a);assertThat(notices.toString()).doesNotContain(b.id().toString(),"Private anonymous reply marker");
  db.update("UPDATE comments SET status='HIDDEN' WHERE id=?",UUID.fromString(c));assertThat(read("/api/v1/notifications",a).toString()).doesNotContain(c);
 }
}
