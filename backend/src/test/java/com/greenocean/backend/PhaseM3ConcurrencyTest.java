package com.greenocean.backend;

import com.greenocean.backend.community.service.*;
import com.greenocean.backend.community.dto.CreateCommunityEventRequest;
import com.greenocean.backend.common.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import java.util.*;import java.util.concurrent.*;import java.time.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class PhaseM3ConcurrencyTest extends PhaseLFixtures {
 @Autowired GroupActivities activities;@Autowired GroupSafety safety;@Autowired GroupMemberships memberships;
 List<UUID> people;UUID group,event,owner,staff;
 @BeforeEach void isolated(){people=new ArrayList<>();for(int i=0;i<5;i++){UUID id=UUID.randomUUID();people.add(id);db.update("INSERT INTO users(id,email,password_hash,status,email_verified) VALUES(?,?,'synthetic-only','ACTIVE',TRUE)",id,"m3-race-"+id+"@example.test");}owner=people.getFirst();staff=people.getLast();db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='MODERATOR'",staff);group=UUID.randomUUID();db.update("INSERT INTO communities(id,name,slug,privacy_type,created_by) VALUES(?,?,?,'PUBLIC',?)",group,"Synthetic M3 race "+group,"g-"+group,owner);for(UUID user:people)db.update("INSERT INTO community_members(community_id,user_id,role,rules_version_ack) VALUES(?,?,?,1)",group,user,user.equals(owner)?"OWNER":"MEMBER");event=activities.save(group,owner,null,0,new CreateCommunityEventRequest("Synthetic capacity circle",null,"TEXT_CIRCLE",Instant.now().plusSeconds(3600),Instant.now().plusSeconds(7200),1));}
 @AfterEach void cleanup(){db.update("DELETE FROM community_lifecycle_decisions WHERE community_id=?",group);db.update("DELETE FROM communities WHERE id=?",group);for(UUID user:people){db.update("DELETE FROM notifications WHERE user_id=?",user);db.update("DELETE FROM audit_logs WHERE actor_user_id=?",user);db.update("DELETE FROM users WHERE id=?",user);}}
 List<String> race(List<Callable<String>> actions)throws Exception{var pool=Executors.newFixedThreadPool(actions.size());var latch=new CountDownLatch(1);try{var tasks=actions.stream().map(action->pool.submit(()->{latch.await();return action.call();})).toList();latch.countDown();List<String> results=new ArrayList<>();for(var task:tasks)results.add(task.get(25,TimeUnit.SECONDS));return results;}finally{pool.shutdownNow();pool.awaitTermination(10,TimeUnit.SECONDS);}}
 String attend(UUID user){try{activities.attendance(group,event,user,true);return "JOINED";}catch(ConflictException|NotFoundException denied){return "DENIED";}}
 @Test void fourContendersCannotOverbookTheLastSeat()throws Exception{List<Callable<String>> calls=people.subList(1,5).stream().<Callable<String>>map(user->()->attend(user)).toList();assertThat(race(calls)).containsExactlyInAnyOrder("JOINED","DENIED","DENIED","DENIED");assertThat(db.queryForObject("SELECT count(*) FROM community_event_attendees WHERE event_id=?",Integer.class,event)).isEqualTo(1);}
 @Test void suspensionAndRegistrationHaveOneSerializedOrder()throws Exception{race(List.of(()->attend(people.get(1)),()->{safety.lifecycle(group,staff,"SUSPENDED","Synthetic race suspension",true);return "SUSPENDED";}));assertThat(db.queryForObject("SELECT status FROM community_events WHERE id=?",String.class,event)).isEqualTo("CANCELLED");assertThat(db.queryForObject("SELECT active_group_member(?,?)",Boolean.class,group,people.get(1))).isFalse();assertThatThrownBy(()->activities.attendance(group,event,people.get(2),true)).isInstanceOf(NotFoundException.class);}
 @Test void transferAndSuspensionKeepOneAuthoritativeOwner()throws Exception{UUID target=people.get(1),transfer=memberships.transfer(group,owner,target);race(List.of(()->{try{memberships.acceptTransfer(transfer,target,true);return "TRANSFERRED";}catch(ConflictException|NotFoundException denied){return "DENIED";}},()->{safety.lifecycle(group,staff,"SUSPENDED","Synthetic ownership race",true);return "SUSPENDED";}));assertThat(db.queryForObject("SELECT count(*) FROM community_members WHERE community_id=? AND role='OWNER'",Integer.class,group)).isEqualTo(1);assertThat(db.queryForObject("SELECT status FROM communities WHERE id=?",String.class,group)).isEqualTo("SUSPENDED");}
 @Test void memberRemovalAndAttendanceCannotLeaveUnauthorizedAttendees()throws Exception{UUID target=people.get(1);race(List.of(()->attend(target),()->{memberships.remove(group,target,owner,"BAN","Synthetic capacity removal",null);return "REMOVED";}));assertThat(db.queryForObject("SELECT count(*) FROM community_event_attendees WHERE event_id=? AND user_id=?",Integer.class,event,target)).isZero();assertThat(db.queryForObject("SELECT active_group_member(?,?)",Boolean.class,group,target)).isFalse();}
}
