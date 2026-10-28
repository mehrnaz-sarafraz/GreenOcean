package com.greenocean.backend.community.service;
import com.greenocean.backend.common.exception.*;
import com.greenocean.backend.identity.AccessPolicy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
@Service
public class GroupPolicy {
 public final JdbcTemplate db;private final AccessPolicy access;
 public GroupPolicy(JdbcTemplate db,AccessPolicy access){this.db=db;this.access=access;}
 public void eligible(UUID user){access.require(user,"COMMUNITY");if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM users WHERE id=? AND status='ACTIVE' AND email_verified AND deleted_at IS NULL)",Boolean.class,user)))throw new ForbiddenException("Active verified membership eligibility is required");}
 public Map<String,Object> lock(UUID group){var rows=db.queryForList("SELECT * FROM communities WHERE id=? FOR UPDATE",group);if(rows.isEmpty())missing();return rows.getFirst();}
 public void visible(UUID group,UUID user){if(!Boolean.TRUE.equals(db.queryForObject("SELECT visible_group(?,?)",Boolean.class,group,user)))missing();}
 public String member(UUID group,UUID user){if(!Boolean.TRUE.equals(db.queryForObject("SELECT active_group_member(?,?)",Boolean.class,group,user)))missing();return db.queryForObject("SELECT role FROM community_members WHERE community_id=? AND user_id=?",String.class,group,user);}
 public String manager(UUID group,UUID user){String role=member(group,user);if(!Set.of("OWNER","MODERATOR").contains(role))throw new ForbiddenException("Group manager permission is required");return role;}
 public void owner(UUID group,UUID user){if(!"OWNER".equals(member(group,user)))throw new ForbiddenException("Group owner permission is required");}
 public void writable(UUID group){if(!"ACTIVE".equals(db.queryForObject("SELECT status FROM communities WHERE id=?",String.class,group)))throw new ConflictException("Group interaction is unavailable");}
 public void staff(UUID user){eligible(user);if(!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=? AND r.name IN('ADMIN','MODERATOR'))",Boolean.class,user)))throw new ForbiddenException("Current platform reviewer authorization is required");}
 public void audit(UUID actor,UUID group,String action){db.update("INSERT INTO audit_logs(actor_user_id,action,entity_type,entity_id) VALUES(?,?,'COMMUNITY',?)",actor,action,group);}
 public void notify(UUID user){db.update("INSERT INTO notifications(user_id,type,title,body) VALUES(?,'SYSTEM_NOTICE','Group update','Open your group workspace to review the update.')",user);}
 public void bound(UUID actor,String kind,int limit){db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))","group-budget:"+actor+":"+kind);if(db.queryForObject("SELECT count(*) FROM audit_logs WHERE actor_user_id=? AND action=? AND created_at>CURRENT_TIMESTAMP-INTERVAL '1 hour'",Integer.class,actor,kind)>=limit)throw new TooManyRequestsException("Please pause before trying again",3600);audit(actor,null,kind);}
 public static void missing(){throw new NotFoundException("Community was not found");}
}
