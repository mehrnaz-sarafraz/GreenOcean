package com.greenocean.backend;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import tools.jackson.databind.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseGHPrivacyIntegrationTest {
 @Autowired MockMvc mvc; @Autowired JdbcTemplate db; @Autowired ObjectMapper json;
 @Autowired jakarta.persistence.EntityManager entities;
 @Autowired org.springframework.transaction.PlatformTransactionManager manager;
 @BeforeEach void clearSyntheticRateWindows() {
  var tx=new org.springframework.transaction.support.TransactionTemplate(manager);
  tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  tx.executeWithoutResult(status->db.update("DELETE FROM auth_rate_windows"));
 }
 record Member(UUID id,String username,String token,String email) {}
 Member member() throws Exception {
  String email="gh-"+UUID.randomUUID()+"@example.test";
  mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("email",email,"password","Synthetic GH passphrase 2026","countryCode","IR","adultSelfAttested",true,"termsVersion",1)))).andExpect(status().isCreated());
  PhaseAFixtures.activate(db,email);entities.clear();
  var result=mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("email",email,"password","Synthetic GH passphrase 2026")))).andExpect(status().isOk()).andReturn();
  UUID id=db.queryForObject("SELECT id FROM users WHERE email=?",UUID.class,email);
  return new Member(id,db.queryForObject("SELECT username FROM profiles WHERE user_id=?",String.class,id),json.readTree(result.getResponse().getContentAsString()).get("accessToken").asText(),email);
 }
 JsonNode read(String path,Member m) throws Exception {return json.readTree(mvc.perform(get(path).header("Authorization","Bearer "+m.token)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());}
 JsonNode change(String path,Member m,Map<String,Object> data) throws Exception {return json.readTree(mvc.perform(patch(path).header("Authorization","Bearer "+m.token).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(data))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());}
 String publish(Member m,String audience,boolean anonymous,UUID community,String warning) throws Exception {
  var body=new HashMap<String,Object>();body.put("body","GH synthetic audience marker "+UUID.randomUUID());body.put("visibility",audience);body.put("anonymous",anonymous);body.put("communityId",community);body.put("contentWarning",warning);body.put("broaderAudienceConfirmed",true);
  return json.readTree(mvc.perform(post("/api/v1/posts").header("Authorization","Bearer "+m.token).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();
 }
 void denied(String path,Member m) throws Exception {mvc.perform(get(path).header("Authorization","Bearer "+m.token)).andExpect(status().isNotFound());}
 void follow(Member viewer,Member author){db.update("INSERT INTO follows(follower_id,following_id) VALUES (?,?) ON CONFLICT DO NOTHING",viewer.id,author.id);}
 @Test @Tag("happy") void minimalRegistrationHasOnePrivateProfileAndNonEmailIdentity() throws Exception {
  var a=member();var b=member();var p=read("/api/v1/profiles/me",a);var prefs=read("/api/v1/preferences/me",a);
  assertThat(p.get("profilePrivate").asBoolean()).isTrue();assertThat(p.get("showLocation").asBoolean()).isFalse();assertThat(p.get("showBirthYear").asBoolean()).isFalse();
  assertThat(a.username).startsWith("member_").doesNotContain("@","gh-").isNotEqualTo(b.username);
  assertThat(read("/api/v1/profiles/me",b).get("displayName").asText()).isEqualTo(p.get("displayName").asText());
  assertThat(prefs.get("onboardingStatus").asText()).isEqualTo("NOT_STARTED");assertThat(prefs.get("allowMessageRequests").asBoolean()).isFalse();
  mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("email",a.email,"password","Synthetic GH passphrase 2026","countryCode","IR","adultSelfAttested",true,"termsVersion",1)))).andExpect(status().isCreated());
  assertThat(db.queryForObject("SELECT count(*) FROM profiles WHERE user_id=?",Integer.class,a.id)).isEqualTo(1);
 }
 @Test @Tag("malicious") void privateFieldsAreRedactedInProfileAndSearchButVisibleToApprovedFollower() throws Exception {
  var a=member();var b=member();change("/api/v1/profiles/me",a,Map.of("bio","Private bio marker","countryCode","IR","city","Tehran","birthYear",1990,"showLocation",true,"showBirthYear",true));
  String path="/api/v1/profiles/"+a.username;
  var hidden=read(path,b);assertThat(hidden.toString()).doesNotContain("Private bio marker","Tehran","1990",a.email,"supportTopics");
  assertThat(read("/api/v1/search/users?q="+a.username,b).toString()).doesNotContain("Private bio marker");
  follow(b,a);assertThat(read(path,b).get("bio").asText()).isEqualTo("Private bio marker");
  db.update("INSERT INTO blocks(blocker_id,blocked_id) VALUES (?,?)",b.id,a.id);denied(path,b);
  assertThat(read("/api/v1/search/users?q="+a.username,b).get("items").isEmpty()).isTrue();
 }
 @Test @Tag("creative") void persianNamesDuplicateDisplayAndAvatarClearPreserveAccountIdentity() throws Exception {
  var a=member();var b=member();String name="دریای سبز 🌊";
  change("/api/v1/profiles/me",a,Map.of("displayName",name,"avatarUrl","https://images.example.test/synthetic.png"));change("/api/v1/profiles/me",b,Map.of("displayName",name));
  var cleared=change("/api/v1/profiles/me",a,Map.of("avatarUrl","","bio",""));assertThat(cleared.get("displayName").asText()).isEqualTo(name);assertThat(cleared.get("avatarUrl").isNull()).isTrue();
  assertThat(db.queryForObject("SELECT email FROM users WHERE id=?",String.class,a.id)).isEqualTo(a.email);
 }
 @Test @Tag("malicious") void rejectsReservedTakenUnsafeIdentityAndAvatarWithoutPersisting() throws Exception {
  var a=member();var b=member();for(var payload:List.of(Map.of("username","admin"),Map.of("username","bad name"),Map.of("displayName","bad\u202ename"),Map.of("avatarUrl","file:///C:/private.png"),Map.of("avatarUrl","javascript:alert(1)"),Map.of("avatarUrl","https://user:pass@example.test/a")))
   mvc.perform(patch("/api/v1/profiles/me").header("Authorization","Bearer "+a.token).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload))).andExpect(status().isBadRequest());
  mvc.perform(patch("/api/v1/profiles/me").header("Authorization","Bearer "+a.token).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("username",b.username)))).andExpect(status().isConflict());
  assertThat(read("/api/v1/profiles/me",a).get("username").asText()).isEqualTo(a.username);
 }
 @Test @Tag("malicious") void audienceMatrixSurvivesPrivacyTransitionsAndAlternateApiRequests() throws Exception {
  var a=member();var viewer=member();var stranger=member();follow(viewer,a);
  String restricted=publish(a,"FOLLOWERS",false,null,null), broad=publish(a,"PUBLIC",false,null,null);
  for(boolean privacy:List.of(false,true,false,true)) {
   change("/api/v1/profiles/me",a,Map.of("profilePrivate",privacy));
   read("/api/v1/posts/"+restricted,viewer);denied("/api/v1/posts/"+restricted,stranger);read("/api/v1/posts/"+broad,stranger);
   assertThat(read("/api/v1/posts/feed?size=100",stranger).toString()).doesNotContain(restricted);
   assertThat(read("/api/v1/search/posts?q=GH%20synthetic&size=100",stranger).toString()).doesNotContain(restricted);
   mvc.perform(get("/api/v1/posts/"+restricted+"/comments").header("Authorization","Bearer "+stranger.token)).andExpect(status().isNotFound());
   for(String action:List.of("like","bookmark"))mvc.perform(put("/api/v1/posts/"+restricted+"/"+action).header("Authorization","Bearer "+stranger.token)).andExpect(status().isNotFound());
  }
  db.update("DELETE FROM follows WHERE follower_id=? AND following_id=?",viewer.id,a.id);denied("/api/v1/posts/"+restricted,viewer);
  db.update("INSERT INTO blocks(blocker_id,blocked_id) VALUES (?,?)",a.id,stranger.id);denied("/api/v1/posts/"+broad,stranger);
  db.update("DELETE FROM blocks WHERE blocker_id=? AND blocked_id=?",a.id,stranger.id);read("/api/v1/posts/"+broad,stranger);
  mvc.perform(delete("/api/v1/posts/"+broad).header("Authorization","Bearer "+a.token)).andExpect(status().isNoContent());denied("/api/v1/posts/"+broad,stranger);
 }
 @Test @Tag("malicious") void broaderAudienceRequiresExplicitPerPostConfirmation() throws Exception {
  var a=member();mvc.perform(post("/api/v1/posts").header("Authorization","Bearer "+a.token).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("body","No accidental broad publication","visibility","PUBLIC")))).andExpect(status().isBadRequest());
  assertThat(db.queryForObject("SELECT count(*) FROM posts WHERE author_id=?",Integer.class,a.id)).isZero();
 }
 @Test @Tag("malicious") void communityMembershipCannotBeReplacedByFollowAndLeavingRevokesAccess() throws Exception {
  var a=member();var b=member();follow(b,a);
  UUID community=db.queryForObject("INSERT INTO communities(name,slug,is_private,created_by) VALUES ('GH synthetic',?,TRUE,?) RETURNING id",UUID.class,"gh-"+UUID.randomUUID(),a.id);
  db.update("INSERT INTO community_members(community_id,user_id,role) VALUES (?,?,'OWNER')",community,a.id);
  String id=publish(a,"COMMUNITY",false,community,null);denied("/api/v1/posts/"+id,b);
  db.update("INSERT INTO community_members(community_id,user_id) VALUES (?,?)",community,b.id);read("/api/v1/posts/"+id,b);
  db.update("DELETE FROM community_members WHERE community_id=? AND user_id=?",community,b.id);denied("/api/v1/posts/"+id,b);
 }
 @Test @Tag("malicious") void anonymousPostsDoNotAppearInOtherMembersAuthorListingsOrCounts() throws Exception {
  var a=member();var b=member();follow(b,a);String id=publish(a,"FOLLOWERS",true,null,null);
  assertThat(read("/api/v1/posts/"+id,b).get("author").isNull()).isTrue();
  assertThat(read("/api/v1/profiles/"+a.username+"/posts",b).toString()).doesNotContain(id);
  assertThat(read("/api/v1/profiles/"+a.username,b).get("postCount").asInt()).isZero();
  assertThat(read("/api/v1/profiles/"+a.username+"/posts",a).toString()).contains(id);
 }
 @Test @Tag("confused") void contentControlsFilterDiscoveryIndependentlyOfAuthorizedDirectLinks() throws Exception {
  var a=member();var b=member();String id=publish(a,"PUBLIC",false,null,"Permitted sensitive note");
  assertThat(read("/api/v1/posts/feed?size=100",b).toString()).doesNotContain(id);read("/api/v1/posts/"+id,b);
  change("/api/v1/preferences/me",b,Map.of("strongerContentControls",false,"blurSensitiveContent",false));assertThat(read("/api/v1/posts/feed?size=100",b).toString()).contains(id);
  change("/api/v1/preferences/me",b,Map.of("mutedTerms",List.of("GH synthetic")));assertThat(read("/api/v1/search/posts?q=GH%20synthetic",b).toString()).doesNotContain(id);read("/api/v1/posts/"+id,b);
  db.update("UPDATE posts SET status='HIDDEN' WHERE id=?",UUID.fromString(id));denied("/api/v1/posts/"+id,b);
 }
 @Test @Tag("happy") void onboardingProgressSkipCompleteAndEmptyClearArePersistedSeparately() throws Exception {
  var a=member();change("/api/v1/preferences/me",a,Map.of("onboardingStatus","IN_PROGRESS","onboardingStep",1,"supportTopics",List.of()));
  assertThat(read("/api/v1/preferences/me",a).get("onboardingStep").asInt()).isEqualTo(1);
  var skipped=change("/api/v1/preferences/me",a,Map.of("onboardingStatus","SKIPPED"));assertThat(skipped.get("onboardingStatus").asText()).isEqualTo("SKIPPED");assertThat(skipped.get("onboardingComplete").asBoolean()).isTrue();
  change("/api/v1/preferences/me",a,Map.of("supportStyle","LISTENER"));var completed=change("/api/v1/preferences/me",a,Map.of("onboardingStatus","COMPLETED","onboardingStep",3,"supportStyle",""));
  assertThat(completed.get("supportStyle").isNull()).isTrue();assertThat(completed.get("onboardingStatus").asText()).isEqualTo("COMPLETED");
  assertThat(read("/api/v1/account/archive",a).get("preferences").get("onboardingStatus").asText()).isEqualTo("COMPLETED");
 }
 @Test @Tag("malicious") void ownerOnlyPreferencesRejectForgedTopicsStylesProgressAndLargePayloads() throws Exception {
  var a=member();var b=member();
  for(var payload:List.of(Map.of("supportTopics",List.of("invented-diagnosis")),Map.of("supportStyle","ADMIN"),Map.of("onboardingStatus","ADMIN"),Map.of("onboardingStep",99),Map.of("onboardingStatus","COMPLETED"),Map.of("supportTopics",Collections.nCopies(31,"anxiety"))))
   mvc.perform(patch("/api/v1/preferences/me").header("Authorization","Bearer "+a.token).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload))).andExpect(status().isBadRequest());
  change("/api/v1/preferences/me",a,Map.of("supportStyle","LISTENER","intendedRole","PROFESSIONAL"));
  assertThat(read("/api/v1/preferences/me",b).get("supportStyle").isNull()).isTrue();assertThat(db.queryForObject("SELECT count(*) FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=? AND r.name='PROFESSIONAL'",Integer.class,a.id)).isZero();
  mvc.perform(get("/api/v1/preferences/"+b.id).header("Authorization","Bearer "+a.token)).andExpect(status().is4xxClientError());
 }
 @Test @Tag("confused") void legacyBooleanDismissalDoesNotFabricateCompletionAndNullDoesNotClear() throws Exception {
  var a=member();change("/api/v1/preferences/me",a,Map.of("onboardingComplete",true,"supportStyle","LISTENER"));
  var patch=new HashMap<String,Object>();patch.put("supportStyle",null);var saved=change("/api/v1/preferences/me",a,patch);
  assertThat(saved.get("onboardingStatus").asText()).isEqualTo("LEGACY_UNKNOWN");assertThat(saved.get("supportStyle").asText()).isEqualTo("LISTENER");
 }
 @Test @Tag("malicious") void messageRequestsFollowApprovalAndEstablishedBlockAreEnforced() throws Exception {
  var a=member();var b=member();String route="/api/v1/conversations/users/"+a.id;
  mvc.perform(post(route).header("Authorization","Bearer "+b.token)).andExpect(status().isForbidden());
  db.update("INSERT INTO follow_requests(requester_id,target_id,status) VALUES (?,?,'PENDING')",a.id,b.id);
  mvc.perform(post(route).header("Authorization","Bearer "+b.token)).andExpect(status().isForbidden());
  follow(a,b);var started=mvc.perform(post(route).header("Authorization","Bearer "+b.token)).andExpect(status().isCreated()).andReturn();
  String id=json.readTree(started.getResponse().getContentAsString()).get("id").asText();
  read("/api/v1/conversations/"+id+"/messages",b);
  db.update("INSERT INTO blocks(blocker_id,blocked_id) VALUES (?,?)",a.id,b.id);
  mvc.perform(post(route).header("Authorization","Bearer "+b.token)).andExpect(status().isForbidden());
  mvc.perform(get("/api/v1/conversations/"+id+"/messages").header("Authorization","Bearer "+b.token)).andExpect(status().isForbidden());
  mvc.perform(post("/api/v1/conversations/"+id+"/messages").header("Authorization","Bearer "+b.token).contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"blocked bypass\"}")).andExpect(status().isForbidden());
  assertThat(read("/api/v1/conversations",b).toString()).doesNotContain(id);
 }
 @Test @Tag("happy") void enabledRequestsPermitContactAndDeletionErasesPreferencesProgressAndAvatar() throws Exception {
  var a=member();var b=member();change("/api/v1/preferences/me",a,Map.of("allowMessageRequests",true,"supportStyle","LISTENER","onboardingStatus","SKIPPED"));
  mvc.perform(post("/api/v1/conversations/users/"+a.id).header("Authorization","Bearer "+b.token)).andExpect(status().isCreated());
  change("/api/v1/profiles/me",a,Map.of("avatarUrl","https://images.example.test/synthetic.png"));
  mvc.perform(delete("/api/v1/account").header("Authorization","Bearer "+a.token).contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"Synthetic GH passphrase 2026\"}")).andExpect(status().isNoContent());
  assertThat(db.queryForObject("SELECT count(*) FROM user_preferences WHERE user_id=?",Integer.class,a.id)).isZero();
  assertThat(db.queryForObject("SELECT avatar_url FROM profiles WHERE user_id=?",String.class,a.id)).isNull();
  mvc.perform(get("/api/v1/preferences/me").header("Authorization","Bearer "+a.token)).andExpect(status().isUnauthorized());
 }
}
