package com.greenocean.backend;

import com.greenocean.backend.moderation.service.SocialReviewService;
import com.greenocean.backend.moderation.service.AdminService;
import com.greenocean.backend.moderation.dto.ModerationActionRequest;
import com.greenocean.backend.common.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseL3ReviewTest extends PhaseLFixtures {
 @Autowired SocialReviewService reviews; @Autowired AdminService admin;
 UUID staff(String role)throws Exception{var member=member();db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name=?",member.id(),role);return member.id();}
 UUID assessment(String post){return db.queryForObject("SELECT id FROM social_assessments WHERE post_id=? ORDER BY revision DESC LIMIT 1",UUID.class,UUID.fromString(post));}
 String held(Member member)throws Exception{return publish(member,"A synthetic story referencing https://example.test",true).path("id").asText();}
 @Test @Tag("happy") void reviewPublishesExactRevisionWithoutDisclosingAuthor()throws Exception{
  var author=member();var reader=member();UUID reviewer=staff("MODERATOR");String post=held(author);UUID decision=assessment(post);
  mvc.perform(get("/api/v1/posts/"+post).header("Authorization","Bearer "+reader.token())).andExpect(status().isNotFound());
  assertThat(reviews.queue(reviewer,0).toString()).contains(post).doesNotContain(author.id().toString(),author.email());
  reviews.decide(reviewer,decision,1,"ALLOW","This synthetic context is permitted.");
  String response=read("/api/v1/posts/"+post,reader).toString();assertThat(response).doesNotContain(author.id().toString(),author.email());
  String notifications=read("/api/v1/notifications",author).toString();assertThat(notifications).contains("Content review updated").doesNotContain(reviewer.toString(),"This synthetic context is permitted.");
  String notice=read("/api/v1/social-moderation/me",author).toString();assertThat(notice).contains("This synthetic context is permitted.").doesNotContain(reviewer.toString());
  assertThat(db.queryForObject("SELECT count(*) FROM audit_logs WHERE actor_user_id=? AND action='SOCIAL_ALLOW'",Integer.class,reviewer)).isEqualTo(1);
 }
 @Test @Tag("malicious") void staleApprovalCannotPublishEditedBodyOrBeReplayed()throws Exception{
  var author=member();UUID reviewer=staff("MODERATOR");String post=held(author);UUID old=assessment(post);
  db.update("UPDATE posts SET revision=revision+1,body='Unreviewed new text' WHERE id=?",UUID.fromString(post));
  assertThatThrownBy(()->reviews.decide(reviewer,old,1,"ALLOW","Stale approval attempt")).isInstanceOf(ConflictException.class);
  assertThat(db.queryForObject("SELECT status FROM posts WHERE id=?",String.class,UUID.fromString(post))).isEqualTo("UNDER_REVIEW");
 }
 @Test @Tag("malicious") void ordinaryBearerAndSelfReviewCannotApprove()throws Exception{
  var author=member();String post=held(author);UUID id=assessment(post);
  submit("/api/v1/admin/social-moderation/"+id+"/decision",author,Map.of("revision",1,"outcome","ALLOW","note","Forged approval"),403);
  assertThatThrownBy(()->reviews.queue(author.id(),0)).isInstanceOf(ForbiddenException.class);
  db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='MODERATOR'",author.id());
  assertThatThrownBy(()->reviews.decide(author.id(),id,1,"ALLOW","Self approval")).isInstanceOf(ForbiddenException.class);
 }
 @Test @Tag("happy") void ownedAppealRequiresIndependentReviewAndCanRestoreContent()throws Exception{
  var author=member();var other=member();UUID first=staff("MODERATOR"),second=staff("MODERATOR");String post=held(author);UUID id=assessment(post);
  reviews.decide(first,id,1,"BLOCK_PROHIBITED","Please request a second review if context was missed.");
  submit("/api/v1/social-moderation/"+id+"/appeal",other,Map.of("reason","Not my decision"),404);
  submit("/api/v1/social-moderation/"+id+"/appeal",author,Map.of("reason","This is a synthetic quotation."),201);
  submit("/api/v1/social-moderation/"+id+"/appeal",author,Map.of("reason","Duplicate appeal"),409);
  assertThatThrownBy(()->reviews.decide(first,id,1,"ALLOW","Own decision appeal")).isInstanceOf(ForbiddenException.class);
  assertThatThrownBy(()->reviews.beginReview(first,id,1)).isInstanceOf(ForbiddenException.class);
  reviews.beginReview(second,id,1);assertThat(reviews.mine(author.id()).toString()).contains("REVIEWING");
  reviews.decide(second,id,1,"ALLOW","Independent context review permits the text.");
  assertThat(reviews.mine(author.id()).toString()).contains("OVERTURNED").doesNotContain(first.toString(),second.toString());
  read("/api/v1/posts/"+post,other);
  assertThat(db.queryForObject("SELECT count(*) FROM social_review_decisions WHERE assessment_id=?",Integer.class,id)).isEqualTo(2);
  assertThat(db.queryForObject("SELECT initial_outcome FROM social_assessments WHERE id=?",String.class,id)).isEqualTo("HOLD_FOR_REVIEW");
  String archive=read("/api/v1/account/archive",author).toString();assertThat(archive).contains("Independent context review permits the text.","appeal_reason").doesNotContain(first.toString(),second.toString());
  assertThatThrownBy(()->reviews.decide(first,id,1,"BLOCK_PROHIBITED","Replay after closure")).isInstanceOf(ConflictException.class);
 }
 @Test @Tag("malicious") void anonymousAttributionNeedsAdminAndRecordsPurpose()throws Exception{
  var author=member();UUID moderator=staff("MODERATOR"),administrator=staff("ADMIN");UUID id=assessment(held(author));
  assertThatThrownBy(()->reviews.attribution(moderator,id,"Synthetic safety investigation")).isInstanceOf(ForbiddenException.class);
  assertThat(reviews.attribution(administrator,id,"Synthetic safety investigation").get("authorId")).isEqualTo(author.id());
  assertThat(db.queryForObject("SELECT metadata::text FROM audit_logs WHERE entity_id=? AND action='SOCIAL_IDENTITY_REVIEW'",String.class,id)).contains("Synthetic safety investigation");
  db.update("DELETE FROM user_roles WHERE user_id=?",administrator);
  assertThatThrownBy(()->reviews.attribution(administrator,id,"Stale privilege attempt")).isInstanceOf(ForbiddenException.class);
 }
 @Test @Tag("confused") void revisionRequestRemainsPrivateAndExposesRespectfulNotice()throws Exception{
  var author=member();var reader=member();UUID reviewer=staff("MODERATOR");String post=held(author);UUID id=assessment(post);
  reviews.decide(reviewer,id,1,"REQUEST_REVISION","Please remove identifying contact details.");
  assertThat(read("/api/v1/social-moderation/me",author).toString()).contains("REQUEST_REVISION","Please remove identifying contact details.");
  assertThat(read("/api/v1/social-moderation/me",reader).toString()).doesNotContain(id.toString());
  assertThat(read("/api/v1/posts/discovery",reader).toString()).doesNotContain(post);
 }
 @Test void deletedContentCannotBeApprovedOrAppealed()throws Exception{
  var author=member();UUID reviewer=staff("MODERATOR");String post=held(author);UUID id=assessment(post);
  mvc.perform(delete("/api/v1/posts/"+post).header("Authorization","Bearer "+author.token())).andExpect(status().isNoContent());
  assertThatThrownBy(()->reviews.decide(reviewer,id,1,"ALLOW","Deleted item attempt")).isInstanceOf(NotFoundException.class);
  submit("/api/v1/social-moderation/"+id+"/appeal",author,Map.of("reason","Deleted item appeal"),404);
 }
 @Test void reportRemovalActuallyHidesBookmarkedContent()throws Exception{
  var author=member();var reporter=member();UUID reviewer=staff("MODERATOR");String post=publish(author,"A synthetic report removal scenario",true).path("id").asText();
  mvc.perform(put("/api/v1/posts/"+post+"/bookmark").header("Authorization","Bearer "+reporter.token())).andExpect(status().isNoContent());
  UUID report=UUID.fromString(submit("/api/v1/reports",reporter,Map.of("targetType","POST","targetId",post,"reason","OTHER","description","Synthetic report"),201).path("id").asText());
  admin.report(report,reviewer,new ModerationActionRequest("REMOVE","Removed following contextual review"));
  assertThat(read("/api/v1/posts/discovery?mode=SAVED",reporter).toString()).doesNotContain(post);
  mvc.perform(get("/api/v1/posts/"+post).header("Authorization","Bearer "+reporter.token())).andExpect(status().isNotFound());
 }
 @Test void appealAndDecisionNotesAreErasedWithTheAccount()throws Exception{
  var author=member();UUID id=assessment(held(author));submit("/api/v1/social-moderation/"+id+"/appeal",author,Map.of("reason","Sensitive synthetic appeal marker"),201);
  mvc.perform(delete("/api/v1/account").header("Authorization","Bearer "+author.token()).contentType("application/json").content(json.writeValueAsString(Map.of("password","Synthetic L passphrase 2026")))).andExpect(status().isNoContent());
  assertThat(db.queryForObject("SELECT count(*) FROM moderation_appeals WHERE appellant_user_id=?",Integer.class,author.id())).isZero();
  assertThat(db.queryForObject("SELECT count(*) FROM social_assessments WHERE id=?",Integer.class,id)).isZero();
 }
 @Test void publicationEditFloodUsesBoundedServerWindow()throws Exception{
  var author=member();String post=publish(author,"Initial edit flood story",true).path("id").asText();
  for(int revision=1;revision<=9;revision++)mvc.perform(put("/api/v1/posts/"+post).header("Authorization","Bearer "+author.token()).contentType("application/json").content(json.writeValueAsString(Map.of("body","Synthetic revision "+revision,"revision",revision)))).andExpect(status().isOk());
  mvc.perform(put("/api/v1/posts/"+post).header("Authorization","Bearer "+author.token()).contentType("application/json").content(json.writeValueAsString(Map.of("body","Excess synthetic revision","revision",10)))).andExpect(status().isConflict());
 }
 @Test void repeatedReportsAreBoundedWithoutPublishingOrSuspendingAnything()throws Exception{
  var author=member();var reporter=member();String post=publish(author,"Report rate scenario",true).path("id").asText();
  db.update("INSERT INTO reports(reporter_id,post_id,reason,status,resolved_at) SELECT ?,?,'OTHER','RESOLVED',CURRENT_TIMESTAMP FROM generate_series(1,20)",reporter.id(),UUID.fromString(post));
  submit("/api/v1/reports",reporter,Map.of("targetType","POST","targetId",post,"reason","OTHER"),409);
  read("/api/v1/posts/"+post,reporter);assertThat(db.queryForObject("SELECT status FROM users WHERE id=?",String.class,reporter.id())).isEqualTo("ACTIVE");
 }
 @Test void genericAppealEndpointCannotBypassLinkedRevisionReview()throws Exception{
  var author=member();UUID reviewer=staff("MODERATOR"),id=assessment(held(author));UUID appeal=reviews.appeal(author.id(),id,"Synthetic appeal");
  assertThatThrownBy(()->admin.appeal(appeal,reviewer,new ModerationActionRequest("OVERTURN_APPEAL","Bypass attempt"))).isInstanceOf(ConflictException.class);
  assertThat(db.queryForObject("SELECT status FROM moderation_appeals WHERE id=?",String.class,appeal)).isEqualTo("OPEN");
 }
 @Test @Tag("creative") void heldAnonymousCommentCanBeReviewedWithoutExposingItsAuthor()throws Exception{
  var author=member();var responder=member();UUID reviewer=staff("MODERATOR");String post=publish(author,"Reply moderation story",false).path("id").asText();
  String comment=submit("/api/v1/posts/"+post+"/comments",responder,Map.of("body","An example https://example.test","anonymous",true),201).path("id").asText();
  UUID id=db.queryForObject("SELECT id FROM social_assessments WHERE comment_id=?",UUID.class,UUID.fromString(comment));
  assertThat(read("/api/v1/posts/"+post+"/comments",author).toString()).doesNotContain(comment);
  reviews.decide(reviewer,id,1,"ALLOW","Permitted context.");
  assertThat(read("/api/v1/posts/"+post+"/comments",author).toString()).contains(comment).doesNotContain(responder.id().toString());
 }
}
