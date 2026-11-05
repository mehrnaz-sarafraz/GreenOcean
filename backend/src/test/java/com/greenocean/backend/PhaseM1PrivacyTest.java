package com.greenocean.backend;
import com.greenocean.backend.community.service.*;
import com.greenocean.backend.common.exception.*;
import org.junit.jupiter.api.*;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import org.springframework.test.context.ActiveProfiles;import org.springframework.transaction.annotation.Transactional;import java.util.*;
import static org.assertj.core.api.Assertions.*;import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties={"greenocean.groups.proposals-enabled=true","greenocean.groups.activation-approved=true"}) @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseM1PrivacyTest extends PhaseLFixtures {
 @Autowired GroupProposals proposals;
 GroupProposals.Input input(String privacy){return new GroupProposals.Input("Synthetic circle "+UUID.randomUUID(),"Supportive peer discussion","Share experiences without clinical claims",privacy,List.of("Respect privacy and platform safety rules"),0);}
 UUID staff()throws Exception{var m=member();db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='MODERATOR'",m.id());return m.id();}
 UUID approved(Member owner,String privacy)throws Exception{UUID id=(UUID)proposals.save(owner.id(),null,input(privacy)).get("id");proposals.transition(owner.id(),id,1,"SUBMIT");proposals.decide(staff(),id,1,"APPROVED","Synthetic independent approval");return db.queryForObject("SELECT community_id FROM community_proposals WHERE id=?",UUID.class,id);}
 @Test @Tag("happy") void proposalIsPrivateUntilIndependentActivation()throws Exception{
  var owner=member();var outsider=member();var p=proposals.save(owner.id(),null,input("PUBLIC"));UUID id=(UUID)p.get("id");
  assertThat(read("/api/v1/communities",outsider).toString()).doesNotContain(p.get("name").toString());
  mvc.perform(get("/api/v1/community-proposals/"+id).header("Authorization","Bearer "+outsider.token())).andExpect(status().isNotFound());
  proposals.transition(owner.id(),id,1,"SUBMIT");proposals.decide(staff(),id,1,"APPROVED","Reviewed non-clinical proposal");
  UUID group=db.queryForObject("SELECT community_id FROM community_proposals WHERE id=?",UUID.class,id);assertThat(read("/api/v1/communities",outsider).toString()).contains(group.toString());
  assertThat(db.queryForObject("SELECT role FROM community_members WHERE community_id=? AND user_id=?",String.class,group,owner.id())).isEqualTo("OWNER");
  assertThatThrownBy(()->proposals.decide(staff(),id,1,"APPROVED","Replay")).isInstanceOf(ConflictException.class);
 }
 @Test @Tag("malicious") void hiddenExistenceAndRosterAreProtected()throws Exception{
  var owner=member();var outsider=member();UUID group=approved(owner,"HIDDEN");String slug=db.queryForObject("SELECT slug FROM communities WHERE id=?",String.class,group);
  assertThat(read("/api/v1/communities",owner).toString()).doesNotContain(group.toString());
  for(String route:List.of("/api/v1/communities/slug/"+slug,"/api/v1/communities/"+group+"/details"))mvc.perform(get(route).header("Authorization","Bearer "+outsider.token())).andExpect(status().isNotFound());
  assertThat(read("/api/v1/communities/slug/"+slug,owner).path("privacyType").asText()).isEqualTo("HIDDEN");
 }
 @Test @Tag("malicious") void legacyCreationAndForgedStaffCannotActivate()throws Exception{
  var owner=member();submit("/api/v1/communities",owner,Map.of("name","Unsafe direct activation","slug","unsafe-activation","privateCommunity",false),409);
  UUID id=(UUID)proposals.save(owner.id(),null,input("PRIVATE")).get("id");proposals.transition(owner.id(),id,1,"SUBMIT");
  submit("/api/v1/admin/community-proposals/"+id+"/decision",owner,Map.of("revision",1,"outcome","APPROVED","note","Forged reviewer"),403);
  db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='MODERATOR'",owner.id());assertThatThrownBy(()->proposals.decide(owner.id(),id,1,"APPROVED","Self review")).isInstanceOf(ForbiddenException.class);
 }
 @Test @Tag("creative") void revisionAndCancellationRejectStaleDecisions()throws Exception{
  var owner=member();UUID reviewer=staff();UUID id=(UUID)proposals.save(owner.id(),null,input("PRIVATE")).get("id");proposals.transition(owner.id(),id,1,"SUBMIT");proposals.decide(reviewer,id,1,"NEEDS_REVISION","Clarify the peer-support purpose");
  var in=input("PRIVATE");proposals.save(owner.id(),id,new GroupProposals.Input("گروه همدلی آزمایشی",in.description(),in.purpose(),"PRIVATE",in.rules(),1));proposals.transition(owner.id(),id,2,"SUBMIT");
  assertThatThrownBy(()->proposals.decide(reviewer,id,1,"APPROVED","Old revision")).isInstanceOf(ConflictException.class);proposals.transition(owner.id(),id,2,"CANCEL");assertThatThrownBy(()->proposals.decide(reviewer,id,2,"APPROVED","Cancelled")).isInstanceOf(ConflictException.class);
 }
 @Test @Tag("malicious") void groupPublicDoesNotMakeDiscussionOrRosterPublic()throws Exception{
  var owner=member();var reader=member();UUID group=approved(owner,"PUBLIC");var payload=payload("An anonymous group story",true);payload.put("visibility","COMMUNITY");payload.put("communityId",group.toString());String post=submit("/api/v1/posts",owner,payload,201).path("id").asText();
  mvc.perform(get("/api/v1/posts/"+post).header("Authorization","Bearer "+reader.token())).andExpect(status().isNotFound());
  db.update("INSERT INTO community_members(community_id,user_id) VALUES(?,?)",group,reader.id());assertThat(read("/api/v1/communities/"+group+"/details",reader).path("members").size()).isZero();assertThat(read("/api/v1/posts/"+post,reader).toString()).doesNotContain(owner.id().toString());
 }
}
