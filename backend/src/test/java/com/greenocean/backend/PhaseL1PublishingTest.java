package com.greenocean.backend;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseL1PublishingTest extends PhaseLFixtures {
 @Test @Tag("confused") void namedPublicationRequiresConsciousIdentity()throws Exception{
  var a=member();var p=payload("No accidental named publication",false);p.remove("publicationIdentity");submit("/api/v1/posts",a,p,400);
  p.put("publicationIdentity","FORGED");submit("/api/v1/posts",a,p,400);
  p.put("publicationIdentity","ANONYMOUS");submit("/api/v1/posts",a,p,400);
 }
 @Test @Tag("happy") void anonymousAndNamedWorkWithoutTopic()throws Exception{
  var a=member();var b=member();var anonymous=publish(a,"A general experience بدون طبقه‌بندی 🌊",true);
  assertThat(anonymous.path("status").asText()).isEqualTo("PUBLISHED");assertThat(anonymous.path("category").isNull()).isTrue();
  var view=read("/api/v1/posts/"+anonymous.path("id").asText(),b).toString();
  assertThat(view).doesNotContain(a.id.toString(),a.email,"member_");
  assertThat(publish(a,"A named experience",false).path("author").path("userId").asText()).isEqualTo(a.id.toString());
 }
 @Test @Tag("malicious") void blockedSubmissionCannotAppearAcrossPublicReads()throws Exception{
  var a=member();var b=member();var p=publish(a,"I will kill you",true);String id=p.path("id").asText();
  assertThat(p.path("safetyOutcome").asText()).isEqualTo("BLOCK_PROHIBITED");
  mvc.perform(get("/api/v1/posts/"+id).header("Authorization","Bearer "+b.token())).andExpect(status().isNotFound());
  assertThat(read("/api/v1/posts/feed",b).toString()).doesNotContain(id);
  assertThat(read("/api/v1/search/posts?q=kill",b).toString()).doesNotContain(id);
  assertThat(read("/api/v1/posts/mine",a).toString()).contains(id);
 }
 @Test @Tag("creative") void bodyEditWithdrawsUnsafeRevisionAndRejectsStaleVersion()throws Exception{
  var a=member();var b=member();String id=publish(a,"A kind reflection",true).path("id").asText();
  mvc.perform(put("/api/v1/posts/"+id).header("Authorization","Bearer "+b.token()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("body","Stolen edit","revision",1)))).andExpect(status().isNotFound());
  mvc.perform(put("/api/v1/posts/"+id).header("Authorization","Bearer "+a.token()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("body","You are worthless","revision",1)))).andExpect(status().isOk()).andExpect(jsonPath("$.safetyOutcome").value("REQUEST_REVISION")).andExpect(jsonPath("$.anonymous").value(true));
  mvc.perform(get("/api/v1/posts/"+id).header("Authorization","Bearer "+b.token())).andExpect(status().isNotFound());
  mvc.perform(put("/api/v1/posts/"+id).header("Authorization","Bearer "+a.token()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("body","Stale content","revision",1)))).andExpect(status().isConflict());
 }
 @Test @Tag("malicious") void duplicateRetryCreatesOnePost()throws Exception{
  var a=member();publish(a,"Unique retry marker",true);submit("/api/v1/posts",a,payload("Unique retry marker",true),409);
  assertThat(db.queryForObject("SELECT count(*) FROM posts WHERE author_id=?",Integer.class,a.id())).isEqualTo(1);
 }
 @Test @Tag("happy") void draftsArePrivateExpiringExportedAndDiscardable()throws Exception{
  var a=member();var b=member();var data=Map.of("payload",Map.of("body","Private draft marker","publicationIdentity","ANONYMOUS","visibility","FOLLOWERS","postType","EXPERIENCE"),"revision",0);
  var draft=submit("/api/v1/posts/drafts",a,data,200);String id=draft.path("id").asText();
  assertThat(draft.path("payload").asText()).contains("Private draft marker");
  mvc.perform(get("/api/v1/posts/drafts/"+id).header("Authorization","Bearer "+b.token())).andExpect(status().isNotFound());
  assertThat(read("/api/v1/posts/drafts",b).isEmpty()).isTrue();
  assertThat(read("/api/v1/account/archive",a).toString()).contains("Private draft marker");
  assertThat(read("/api/v1/account/archive",b).toString()).doesNotContain("Private draft marker");
  assertThat(read("/api/v1/search/posts?q=Private",b).toString()).doesNotContain("Private draft marker");
  db.update("UPDATE social_drafts SET expires_at=CURRENT_TIMESTAMP-INTERVAL '1 second' WHERE id=?",UUID.fromString(id));
  mvc.perform(get("/api/v1/posts/drafts/"+id).header("Authorization","Bearer "+a.token())).andExpect(status().isNotFound());
 }
 @Test void draftPublishConsumesExactlyOnce()throws Exception{
  var a=member();var d=submit("/api/v1/posts/drafts",a,Map.of("payload",Map.of("body","Draft publication"),"revision",0),200);
  String path="/api/v1/posts/drafts/"+d.path("id").asText()+"/publish";submit(path,a,payload("Draft publication",true),200);submit(path,a,payload("Draft publication",true),404);
 }
 @Test void anonymousOwnerCommentDoesNotRevealOwnership()throws Exception{
  var a=member();var b=member();String id=publish(a,"An anonymous story",true).path("id").asText();
  var c=submit("/api/v1/posts/"+id+"/comments",a,Map.of("body","Thank you for listening","anonymous",false),201);
  assertThat(c.path("anonymous").asBoolean()).isTrue();assertThat(read("/api/v1/posts/"+id+"/comments",b).toString()).doesNotContain(a.id().toString(),a.email());
 }
 @ParameterizedTest @CsvSource(delimiter='|',value={
  "I feel sad and need support|ALLOW","I am angry about my difficult day|ALLOW","امروز حالم خوب نیست و نیاز به همدلی دارم|ALLOW",
  "من دارم تلاش می‌کنم 🌊|ALLOW","You are worthless|REQUEST_REVISION","تو احمقی|REQUEST_REVISION",
  "I will kill you|BLOCK_PROHIBITED","خودتو بکش|BLOCK_PROHIBITED","He said kill you to me|HOLD_FOR_REVIEW",
  "He called me worthless|ALLOW","k i l l you|HOLD_FOR_REVIEW","<script>alert(1)</script>|HOLD_FOR_REVIEW",
  "My address is test@example.test|HOLD_FOR_REVIEW","buy now|REQUEST_REVISION","تهدید شدم|HOLD_FOR_REVIEW"})
 void boundedBilingualSafetyExamples(String text,String expected){assertThat(new com.greenocean.backend.post.service.ContentSafety().assess(text).outcome().name()).isEqualTo(expected);}
}
