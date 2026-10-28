package com.greenocean.backend.community.service;

import com.greenocean.backend.common.exception.*;
import com.greenocean.backend.community.dto.CreateCommunityEventRequest;
import com.greenocean.backend.post.dto.CreatePostRequest;
import com.greenocean.backend.post.dto.PostResponse;
import com.greenocean.backend.post.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

/** Group lock precedes event lock for capacity, membership and lifecycle mutations. */
@Service
public class GroupActivities {
 private final boolean enabled;private final GroupPolicy p; private final ContentClassifier safety; private final PostService posts;
 public GroupActivities(GroupPolicy p,ContentClassifier safety,PostService posts,@org.springframework.beans.factory.annotation.Value("${greenocean.groups.activities-enabled:false}")boolean enabled){this.enabled=enabled;this.p=p;this.safety=safety;this.posts=posts;}
 private Map<String,Object> event(UUID group,UUID id){return p.db.queryForList("SELECT * FROM community_events WHERE community_id=? AND id=? FOR UPDATE",group,id).stream().findFirst().orElseThrow(()->new NotFoundException("Community was not found"));}
 private void version(Map<String,Object> e,long revision){if(((Number)e.get("revision")).longValue()!=revision)throw new ConflictException("Activity changed; reload before continuing");}
 private void requireEnabled(){if(!enabled)throw new ForbiddenException("Group activities are unavailable pending operational approval");}
 private void validate(CreateCommunityEventRequest in){requireEnabled();
  if(!Set.of("TEXT_CIRCLE","WORKSHOP","Q_AND_A").contains(in.format())||!in.startsAt().isAfter(Instant.now())||!in.endsAt().isAfter(in.startsAt()))throw new IllegalArgumentException("Choose a future text activity with a valid end time");
  if(safety.assess(in.title()+"\n"+Objects.toString(in.description(),"")).outcome()!=ContentSafety.Outcome.ALLOW)throw new ConflictException("Revise the activity under platform safety standards");
 }
 private OffsetDateTime date(Instant time){return time.atOffset(ZoneOffset.UTC);}
 private Instant instant(Object value){return value instanceof java.sql.Timestamp t?t.toInstant():((OffsetDateTime)value).toInstant();}
 public void notifyMembers(UUID group){p.db.update("INSERT INTO notifications(user_id,type,title,body) SELECT user_id,'SYSTEM_NOTICE','Group update','Open your group workspace to review the update.' FROM community_members WHERE community_id=?",group);}
 @Transactional public UUID save(UUID group,UUID actor,UUID id,long revision,CreateCommunityEventRequest in){
  p.lock(group);p.manager(group,actor);p.writable(group);p.contribution(group,actor);validate(in);p.bound(actor,"GROUP_ACTIVITY_WRITE",20);
  if(id==null){id=UUID.randomUUID();p.db.update("INSERT INTO community_events(id,community_id,host_user_id,title,description,format,starts_at,ends_at,capacity) VALUES(?,?,?,?,?,?,?,?,?)",id,group,actor,in.title().trim(),in.description(),in.format(),date(in.startsAt()),date(in.endsAt()),in.capacity());}
  else{var e=event(group,id);version(e,revision);if(!"SCHEDULED".equals(e.get("status"))||!instant(e.get("starts_at")).isAfter(Instant.now()))throw new ConflictException("Only future scheduled activities can be edited");
   if(in.capacity()!=null&&p.db.queryForObject("SELECT count(*) FROM community_event_attendees WHERE event_id=?",Integer.class,id)>in.capacity())throw new ConflictException("Capacity cannot exclude existing attendees");
   p.db.update("UPDATE community_events SET title=?,description=?,format=?,starts_at=?,ends_at=?,capacity=?,revision=revision+1 WHERE id=?",in.title().trim(),in.description(),in.format(),date(in.startsAt()),date(in.endsAt()),in.capacity(),id);}
  p.audit(actor,group,"GROUP_ACTIVITY_SAVED");notifyMembers(group);return id;
 }
 @Transactional public void attendance(UUID group,UUID id,UUID actor,boolean join){
  p.lock(group);p.member(group,actor);var e=event(group,id);
  if(!join){p.db.update("DELETE FROM community_event_attendees WHERE event_id=? AND user_id=?",id,actor);return;}
  requireEnabled();p.eligible(actor);p.contribution(group,actor);p.bound(actor,"GROUP_ATTENDANCE",60);
  if(!"SCHEDULED".equals(e.get("status"))||!instant(e.get("starts_at")).isAfter(Instant.now())||!Set.of("TEXT_CIRCLE","WORKSHOP","Q_AND_A").contains(e.get("format")))throw new ConflictException("Activity registration is closed");
  UUID host=(UUID)e.get("host_user_id");if(host==null)throw new ConflictException("Activity host is unavailable");p.manager(group,host);
  if(Boolean.TRUE.equals(p.db.queryForObject("SELECT EXISTS(SELECT 1 FROM blocks WHERE (blocker_id=? AND blocked_id=?) OR(blocker_id=? AND blocked_id=?))",Boolean.class,actor,host,host,actor)))GroupPolicy.missing();
  if(Boolean.TRUE.equals(p.db.queryForObject("SELECT EXISTS(SELECT 1 FROM community_event_attendees WHERE event_id=? AND user_id=?)",Boolean.class,id,actor)))return;
  if(e.get("capacity")!=null&&p.db.queryForObject("SELECT count(*) FROM community_event_attendees WHERE event_id=?",Integer.class,id)>=((Number)e.get("capacity")).intValue())throw new ConflictException("This activity has reached capacity");
  p.db.update("INSERT INTO community_event_attendees(event_id,user_id) VALUES(?,?) ON CONFLICT DO NOTHING",id,actor);
 }
 @Transactional public void transition(UUID group,UUID id,UUID actor,long revision,String next){p.lock(group);p.manager(group,actor);var e=event(group,id);version(e,revision);
  if(!Set.of("CANCELLED","COMPLETED").contains(next)||!"SCHEDULED".equals(e.get("status")))throw new ConflictException("Activity transition is unavailable");
  if("COMPLETED".equals(next)&&instant(e.get("ends_at")).isAfter(Instant.now()))throw new ConflictException("The activity has not ended");
  p.db.update("UPDATE community_events SET status=?,revision=revision+1 WHERE id=?",next,id);p.audit(actor,group,"GROUP_ACTIVITY_"+next);notifyMembers(group);
 }
 @Transactional public PostResponse discussion(UUID group,UUID id,UUID actor,long revision,CreatePostRequest in){requireEnabled();p.lock(group);p.manager(group,actor);p.contribution(group,actor);var e=event(group,id);version(e,revision);
  if(!"SCHEDULED".equals(e.get("status"))||instant(e.get("ends_at")).isBefore(Instant.now())||e.get("discussion_post_id")!=null)throw new ConflictException("Activity discussion is unavailable or already created");
  if(in.visibility()!=com.greenocean.backend.post.entity.PostVisibility.COMMUNITY||!group.equals(in.communityId()))throw new IllegalArgumentException("Discussion must use this group audience");
  PostResponse post=posts.create(actor,in);p.db.update("UPDATE community_events SET discussion_post_id=?,revision=revision+1 WHERE id=?",post.id(),id);p.audit(actor,group,"GROUP_ACTIVITY_DISCUSSION");return post;
 }
}
