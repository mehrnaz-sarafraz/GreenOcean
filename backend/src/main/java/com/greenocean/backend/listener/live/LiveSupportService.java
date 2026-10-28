package com.greenocean.backend.listener.live;

import com.greenocean.backend.auth.service.AuthRateLimit;
import com.greenocean.backend.identity.AccessPolicy;
import com.greenocean.backend.common.exception.ForbiddenException;
import com.greenocean.backend.messaging.repository.MessagingRepository;
import com.greenocean.backend.listener.planning.ListenerSupportPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.util.*;

@Service
@Transactional(noRollbackFor=LiveSupportException.class)
public class LiveSupportService {
 private final JdbcTemplate db;
 private final AccessPolicy access;
 private final LiveSupportSettings settings;
 private final AuthRateLimit rates;
 private final MessagingRepository messaging;
 private ListenerSupportPolicy planning;
 @Autowired public void configurePlanning(ListenerSupportPolicy policy){this.planning=policy;}
 public LiveSupportService(JdbcTemplate db,AccessPolicy access,LiveSupportSettings settings,AuthRateLimit rates,MessagingRepository messaging) {
  this.db=db;this.access=access;this.settings=settings;this.rates=rates;this.messaging=messaging;
 }
 public boolean enabled(){return settings.enabled();}
 public int offerSeconds(){return settings.offerSeconds();}
 private LiveSupportException failure(int status,String code,String message){return new LiveSupportException(status,code,message);}
 private void requireEnabled(){if(!enabled())throw failure(503,"LIVE_SERVICE_DISABLED","Live listener support is not currently enabled. Other support paths remain available.");}
 private boolean truth(String sql,Object...args){return Boolean.TRUE.equals(db.queryForObject(sql,Boolean.class,args));}
 private Instant now(){return db.queryForObject("SELECT clock_timestamp()",OffsetDateTime.class).toInstant();}
 private void requireMember(UUID user){
  try{access.require(user,"LISTENER");}catch(ForbiddenException denied){throw failure(403,"SERVICE_FORBIDDEN","This account cannot use live listener support. Public resources remain available.");}
  if(!truth("SELECT live_account_eligible(?)",user))throw failure(403,"SERVICE_FORBIDDEN","Verify account eligibility before requesting live support.");
 }
 private void requireListener(UUID user){requireMember(user);if(!truth("SELECT live_listener_qualified(?)",user))throw failure(403,"LISTENER_NOT_AUTHORIZED","Independent listener practice and conduct approval is required before live availability.");}
 public void reconcile(){
  db.queryForList("SELECT pg_advisory_xact_lock(72491001)");
  if(planning!=null)planning.shutdownDisabled();
  db.queryForList("SELECT expire_live_support()");
  if(!enabled())db.update("UPDATE live_support_requests SET status='ENDED_SAFETY',reason_code='SERVICE_DISABLED',ended_at=clock_timestamp() WHERE status IN ('REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED')");
 }
 @Scheduled(fixedDelayString="#{@liveSupportSettings.sweepMillis()}")
 public void sweep(){reconcile();}
 private boolean occupied(UUID user){return truth("SELECT EXISTS(SELECT 1 FROM live_support_requests WHERE (member_id=? OR listener_id=?) AND status IN ('REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED'))",user,user);}
 private boolean sessionValid(UUID user,UUID session,int version){return truth("SELECT EXISTS(SELECT 1 FROM user_sessions WHERE id=? AND user_id=? AND token_version=? AND revoked_at IS NULL AND session_scope<>'PENDING' AND expires_at>clock_timestamp() AND overall_expires_at>clock_timestamp() AND idle_expires_at>clock_timestamp())",session,user,version);}
 public ListenerAvailability availability(UUID user,UUID session){
  reconcile();requireMember(user);
  var rows=db.queryForList("SELECT * FROM listener_presence WHERE listener_id=?",user);
  boolean qualified=truth("SELECT live_listener_qualified(?)",user);
  String state=!enabled()||!qualified?"UNAVAILABLE":occupied(user)?"BUSY":truth("SELECT live_listener_ready(?)",user)?"AVAILABLE_FOR_REQUESTS":rows.isEmpty()?"OFFLINE":"ONLINE_NOT_AVAILABLE";
  Map<String,Object> p=rows.isEmpty()?Map.of():rows.getFirst();boolean mine=session.equals(p.get("session_id"));
  return new ListenerAvailability(enabled(),state,mine?(UUID)p.get("lease_id"):null,mine?instant(p.get("expires_at")):null,now(),settings.heartbeatSeconds());
 }
 public ListenerAvailability setAvailability(UUID user,UUID session,int version,boolean ready,UUID previousLease){
  reconcile();requireEnabled();requireListener(user);
  if(!sessionValid(user,session,version))throw failure(403,"SESSION_REVOKED","Sign in again before setting availability.");
  var rows=db.queryForList("SELECT * FROM listener_presence WHERE listener_id=?",user);
  if(!ready && !rows.isEmpty() && (!session.equals(rows.getFirst().get("session_id")) || !Objects.equals(previousLease,rows.getFirst().get("lease_id"))))throw failure(409,"STALE_LEASE","Availability changed on another device. Refresh its current state.");
  if(ready && occupied(user))throw failure(409,"LISTENER_BUSY","Finish the current support request or session first.");
  db.update("""
   INSERT INTO listener_presence(listener_id,session_id,token_version,lease_id,ready,expires_at)
   VALUES (?,?,?,?,?,clock_timestamp()+(?*INTERVAL '1 second'))
   ON CONFLICT(listener_id) DO UPDATE SET session_id=EXCLUDED.session_id,token_version=EXCLUDED.token_version,
    lease_id=EXCLUDED.lease_id,ready=EXCLUDED.ready,expires_at=EXCLUDED.expires_at
   """,user,session,version,UUID.randomUUID(),ready,settings.presenceSeconds());
  if(!ready)db.update("UPDATE live_support_requests SET status=CASE WHEN status='REQUESTED' THEN 'DECLINED' ELSE 'CANCELLED' END,reason_code='LISTENER_UNAVAILABLE',ended_at=clock_timestamp() WHERE listener_id=? AND status IN ('REQUESTED','ACCEPTED')",user);
  return availability(user,session);
 }
 public ListenerAvailability heartbeat(UUID user,UUID session,int version,UUID lease){
  reconcile();requireEnabled();requireListener(user);
  if(!sessionValid(user,session,version))throw failure(403,"SESSION_REVOKED","Sign in again before renewing availability.");
  int changed=db.update("UPDATE listener_presence SET expires_at=clock_timestamp()+(?*INTERVAL '1 second') WHERE listener_id=? AND session_id=? AND token_version=? AND lease_id=? AND ready AND expires_at>clock_timestamp()",settings.presenceSeconds(),user,session,version,lease);
  if(changed!=1)throw failure(409,"STALE_LEASE","Availability expired or changed. Explicitly become available again.");
  return availability(user,session);
 }
 public LiveSupportView create(UUID user,UUID clientId,UUID target){
  return createOrigin(user,clientId,target,null,null);
 }
 public LiveSupportView createScheduled(UUID user,UUID clientId,UUID target,UUID booking){
  if(planning==null)throw failure(503,"SUPPORT_PATH_DISABLED","Scheduled support is disabled.");
  planning.require("BOOKING");return createOrigin(user,clientId,target,booking,null);
 }
 public LiveSupportView createQueued(UUID user,UUID clientId,UUID target,UUID entry){
  if(planning==null)throw failure(503,"SUPPORT_PATH_DISABLED","Queue support is disabled.");planning.require("QUEUE");return createOrigin(user,clientId,target,null,entry);
 }
 private LiveSupportView createOrigin(UUID user,UUID clientId,UUID target,UUID booking,UUID queue){
  reconcile();requireEnabled();requireMember(user);
  var retry=db.queryForList("SELECT id,listener_id,source_booking_id,source_queue_id FROM live_support_requests WHERE member_id=? AND client_request_id=?",user,clientId);
  if(!retry.isEmpty()){
   if(target!=null && !target.equals(retry.getFirst().get("listener_id")))throw failure(409,"IDEMPOTENCY_CONFLICT","Use a new request identifier for a different listener.");
   if(!Objects.equals(booking,retry.getFirst().get("source_booking_id"))||!Objects.equals(queue,retry.getFirst().get("source_queue_id")))throw failure(409,"IDEMPOTENCY_CONFLICT","This request identifier belongs to another support pathway.");
   return view((UUID)retry.getFirst().get("id"),user);
  }
  if(occupied(user))throw failure(409,"REQUEST_ALREADY_OPEN","You already have an open live support request or session.");
  if(!rates.allowInCurrentTransaction("live-request:"+user,5,Duration.ofMinutes(1)))throw failure(429,"RATE_LIMITED","Please wait before creating another live request.");
  var candidates=db.queryForList("""
   SELECT p.listener_id FROM listener_presence p
   WHERE p.listener_id<>? AND (CAST(? AS UUID) IS NULL OR p.listener_id=?)
    AND live_listener_ready(p.listener_id) AND NOT live_pair_blocked(?,p.listener_id)
    AND listener_all_capacity_available(?,p.listener_id,?,?)
    AND NOT EXISTS(SELECT 1 FROM live_support_requests r WHERE (r.member_id=p.listener_id OR r.listener_id=p.listener_id) AND r.status IN ('REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED'))
   ORDER BY p.last_assigned_at NULLS FIRST,p.listener_id LIMIT 1
   """,UUID.class,user,target,target,user,user,booking,queue);
  if(candidates.isEmpty())throw failure(409,"NO_AVAILABLE_LISTENER","No live listener is currently available. You can check again or choose another support path.");
  UUID listener=candidates.getFirst(),id=UUID.randomUUID();
  db.update("INSERT INTO live_support_requests(id,member_id,listener_id,client_request_id,expires_at,presence_seconds,reconnect_seconds,source_booking_id,source_queue_id,planned_seconds) VALUES (?,?,?,?,clock_timestamp()+(?*INTERVAL '1 second'),?,?,?,?,?)",id,user,listener,clientId,settings.offerSeconds(),settings.presenceSeconds(),settings.reconnectSeconds(),booking,queue,planning!=null&&planning.planning()?1800:null);
  db.update("UPDATE listener_presence SET last_assigned_at=clock_timestamp() WHERE listener_id=?",listener);
  return view(id,user);
 }
 private Map<String,Object> owned(UUID id,UUID user){
  var rows=db.queryForList("SELECT * FROM live_support_requests WHERE id=? AND (member_id=? OR listener_id=?)",id,user,user);
  if(rows.isEmpty())throw failure(404,"REQUEST_NOT_FOUND","Live support request was not found.");return rows.getFirst();
 }
 public LiveSupportView get(UUID id,UUID user){reconcile();requireMember(user);return view(id,user);}
 public List<LiveSupportView> mine(UUID user){
  reconcile();requireMember(user);
  return db.queryForList("SELECT id FROM live_support_requests WHERE (member_id=? OR listener_id=?) ORDER BY created_at DESC LIMIT 20",UUID.class,user,user).stream().map(id->view(id,user)).toList();
 }
 public List<LiveSupportView> incoming(UUID user){
  reconcile();requireEnabled();requireListener(user);
  return db.queryForList("SELECT id FROM live_support_requests WHERE listener_id=? AND status IN ('REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED') ORDER BY created_at LIMIT 1",UUID.class,user).stream().map(id->view(id,user)).toList();
 }
 public LiveSupportView accept(UUID id,UUID user){
  reconcile();requireEnabled();requireListener(user);var r=owned(id,user);
  if(!user.equals(r.get("listener_id")))throw failure(403,"ACCEPT_FORBIDDEN","Only the targeted listener can accept this request.");
  if(Set.of("ACCEPTED","ACTIVE","INTERRUPTED").contains(r.get("status")))return view(id,user);
  requireState(r,"REQUESTED");
  if(!truth("SELECT live_listener_ready(?)",user))throw failure(409,"LISTENER_UNAVAILABLE","Become available before accepting a live request.");
  int changed=db.update("UPDATE live_support_requests SET status='ACCEPTED',accepted_at=clock_timestamp(),listener_seen_at=clock_timestamp(),activation_expires_at=clock_timestamp()+(?*INTERVAL '1 second') WHERE id=? AND status='REQUESTED' AND expires_at>clock_timestamp() AND live_listener_ready(listener_id)",settings.activationSeconds(),id);
  if(changed!=1){reconcile();throw failure(409,"REQUEST_EXPIRED","This live offer is no longer available.");}
  return view(id,user);
 }
 public LiveSupportView decline(UUID id,UUID user){
  reconcile();requireListener(user);var r=owned(id,user);
  if(!user.equals(r.get("listener_id")))throw failure(403,"DECLINE_FORBIDDEN","Only the targeted listener can decline this offer.");
  if("DECLINED".equals(r.get("status")))return view(id,user);requireState(r,"REQUESTED");
  terminal(id,"DECLINED","LISTENER_DECLINED");return view(id,user);
 }
 public LiveSupportView cancel(UUID id,UUID user){
  reconcile();requireMember(user);var r=owned(id,user);
  if(!user.equals(r.get("member_id")))throw failure(403,"CANCEL_FORBIDDEN","Only the member can cancel this request.");
  if(Set.of("CANCELLED","EXPIRED","DECLINED","ENDED_SAFETY").contains(r.get("status")))return view(id,user);
  if(!Set.of("REQUESTED","ACCEPTED").contains(r.get("status")))throw failure(409,"INVALID_TRANSITION","End an active session instead of cancelling its request.");
  terminal(id,"CANCELLED","MEMBER_CANCELLED");return view(id,user);
 }
 public LiveSupportView activate(UUID id,UUID user){
  reconcile();requireEnabled();requireMember(user);var r=owned(id,user);
  if(!user.equals(r.get("member_id")))throw failure(403,"ACTIVATION_FORBIDDEN","Only the requesting member can enter the accepted session.");
  if("ACTIVE".equals(r.get("status")))return view(id,user);requireState(r,"ACCEPTED");
  UUID conversation=UUID.randomUUID();
  int changed=db.update("UPDATE live_support_requests SET status='ACTIVE',conversation_id=?,activated_at=clock_timestamp(),member_seen_at=clock_timestamp(),listener_seen_at=clock_timestamp() WHERE id=? AND status='ACCEPTED' AND activation_expires_at>clock_timestamp() AND live_listener_ready(listener_id)",conversation,id);
  if(changed!=1){reconcile();throw failure(409,"REQUEST_EXPIRED","The accepted request is no longer available for activation.");}
  messaging.createListenerConversation(conversation,UUID.randomUUID(),user,(UUID)r.get("listener_id"));
  if(r.get("source_booking_id")!=null)db.queryForList("SELECT reconcile_listener_planning()");
  return view(id,user);
 }
 public LiveSupportView pulse(UUID id,UUID user){
  reconcile();requireEnabled();requireMember(user);var r=owned(id,user);
  if(!Set.of("ACCEPTED","ACTIVE","INTERRUPTED").contains(r.get("status")))throw failure(409,"SESSION_NOT_ACTIVE","This support session cannot be resumed.");
  String column=user.equals(r.get("member_id"))?"member_seen_at":"listener_seen_at";
  db.update("UPDATE live_support_requests SET "+column+"=clock_timestamp() WHERE id=?",id);
  db.update("UPDATE live_support_requests SET status='ACTIVE',reason_code=NULL,interrupted_at=NULL WHERE id=? AND status='INTERRUPTED' AND member_seen_at+(presence_seconds*INTERVAL '1 second')>clock_timestamp() AND listener_seen_at+(presence_seconds*INTERVAL '1 second')>clock_timestamp() AND interrupted_at+(reconnect_seconds*INTERVAL '1 second')>clock_timestamp()",id);
  return view(id,user);
 }
 public LiveSupportView end(UUID id,UUID user){
  reconcile();requireMember(user);var r=owned(id,user);
  if(Set.of("COMPLETED","ENDED_SAFETY").contains(r.get("status")))return view(id,user);
  if(!Set.of("ACTIVE","INTERRUPTED").contains(r.get("status")))throw failure(409,"INVALID_TRANSITION","There is no active conversation to end.");
  terminal(id,"COMPLETED","PARTICIPANT_ENDED");return view(id,user);
 }
 public LiveSupportView forConversation(UUID conversation,UUID user){
  reconcile();requireMember(user);
  var ids=db.queryForList("SELECT id FROM live_support_requests WHERE conversation_id=? AND (member_id=? OR listener_id=?)",UUID.class,conversation,user,user);
  if(ids.isEmpty())throw failure(404,"REQUEST_NOT_FOUND","Live support session was not found.");return view(ids.getFirst(),user);
 }
 public void requireSending(UUID conversation,UUID user,UUID clientMessage){
  reconcile();requireEnabled();requireMember(user);
  if(clientMessage==null)throw failure(400,"MESSAGE_KEY_REQUIRED","A retry-safe client message identifier is required for live support.");
  var value=forConversation(conversation,user);
  if(!"ACTIVE".equals(value.status()))throw failure(409,"SESSION_NOT_ACTIVE","Live messages are only permitted during an active session.");
 }
 public void authorizePractice(UUID application,UUID staff,boolean approved,int conductVersion){
  reconcile();
  if(!truth("SELECT EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id JOIN users u ON u.id=ur.user_id WHERE ur.user_id=? AND u.status='ACTIVE' AND r.name IN ('ADMIN','MODERATOR'))",staff))throw failure(403,"STAFF_REQUIRED","Authorized staff review is required.");
  var rows=db.queryForList("SELECT user_id FROM listener_applications WHERE id=? AND status='APPROVED' AND accepted_code",UUID.class,application);
  if(rows.isEmpty())throw failure(409,"APPLICATION_NOT_APPROVED","Approve the completed listener application first.");
  UUID listener=rows.getFirst();if(listener.equals(staff))throw failure(403,"INDEPENDENT_REVIEW_REQUIRED","A listener cannot sign off their own supervised practice.");
  if(!approved){db.update("UPDATE listener_live_authorizations SET revoked_at=clock_timestamp() WHERE application_id=?",application);return;}
  if(!truth("SELECT EXISTS(SELECT 1 FROM policy_documents WHERE policy_key='listener-code' AND version=? AND status='PUBLISHED' AND content IS NOT NULL AND published_at<=clock_timestamp())",conductVersion))throw failure(400,"INVALID_CONDUCT_VERSION","Use the published listener code version.");
  if(truth("SELECT EXISTS(SELECT 1 FROM listener_training_modules m WHERE m.is_active AND NOT EXISTS(SELECT 1 FROM listener_training_progress p WHERE p.user_id=? AND p.module_key=m.module_key))",listener))throw failure(409,"TRAINING_INCOMPLETE","Complete the required listener training first.");
  db.update("INSERT INTO listener_live_authorizations(application_id,listener_id,reviewed_by,conduct_version) VALUES (?,?,?,?) ON CONFLICT(application_id) DO UPDATE SET reviewed_by=EXCLUDED.reviewed_by,conduct_version=EXCLUDED.conduct_version,practice_reviewed_at=clock_timestamp(),revoked_at=NULL",application,listener,staff,conductVersion);
 }
 private void terminal(UUID id,String status,String reason){db.update("UPDATE live_support_requests SET status=?,reason_code=?,ended_at=clock_timestamp() WHERE id=?",status,reason,id);}
 private void requireState(Map<String,Object> row,String state){
  if(!state.equals(row.get("status")))throw failure(409,"EXPIRED".equals(row.get("status"))?"REQUEST_EXPIRED":"CANCELLED".equals(row.get("status"))?"REQUEST_CANCELLED":"INVALID_TRANSITION","This request is no longer available for that action.");
 }
 private Instant instant(Object value){return value==null?null:value instanceof OffsetDateTime offset?offset.toInstant():((java.sql.Timestamp)value).toInstant();}
 private LiveSupportView view(UUID id,UUID user){
  owned(id,user);
  return db.queryForObject("""
   SELECT r.*,clock_timestamp() AS server_time, p.display_name AS peer_name,
   CASE WHEN r.listener_id=? THEN r.member_id ELSE r.listener_id END AS peer_id
   FROM live_support_requests r JOIN profiles p ON p.user_id=CASE WHEN r.listener_id=? THEN r.member_id ELSE r.listener_id END WHERE r.id=?
   """,(rs,n)->new LiveSupportView(rs.getObject("id",UUID.class),rs.getString("status"),rs.getString("reason_code"),time(rs,"server_time"),time(rs,"expires_at"),time(rs,"activation_expires_at"),time(rs,"interrupted_at"),time(rs,"ended_at"),rs.getObject("conversation_id",UUID.class),rs.getObject("peer_id",UUID.class),rs.getString("peer_name"),user.equals(rs.getObject("listener_id",UUID.class)),rs.getInt("presence_seconds"),rs.getInt("reconnect_seconds"),time(rs,"activated_at"),time(rs,"planned_ends_at")),user,user,id);
 }
 private Instant time(ResultSet rs,String column)throws SQLException{var value=rs.getObject(column,OffsetDateTime.class);return value==null?null:value.toInstant();}
}
