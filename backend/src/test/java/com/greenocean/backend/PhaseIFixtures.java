package com.greenocean.backend;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;
final class PhaseIFixtures {
 record Actor(UUID id,UUID session,String token){}
 static Actor member(JdbcTemplate db,UserRepository users,TokenService tokens){
  UUID id=UUID.randomUUID();String email="i-"+id+"@example.test";
  db.update("INSERT INTO users(id,email,password_hash,status,email_verified,phase_a_enrolled) VALUES (?,?,'synthetic-not-a-login-hash','ACTIVE',TRUE,TRUE)",id,email);
  db.update("INSERT INTO profiles(user_id,username,display_name) VALUES (?,?,'Synthetic peer')",id,"i_"+id.toString().replace("-","").substring(0,20));
  db.update("INSERT INTO user_preferences(user_id) VALUES (?) ON CONFLICT DO NOTHING",id);
  db.update("INSERT INTO account_eligibility(user_id,age_result,age_method,residence_country,display_reviewed_at) VALUES (?,'ADULT','SELF_ATTESTED','IR',CURRENT_TIMESTAMP)",id);
  db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='USER'",id);
  return session(db,users,tokens,id);
 }
 static Actor session(JdbcTemplate db,UserRepository users,TokenService tokens,UUID id){
  UUID session=UUID.randomUUID();db.update("INSERT INTO user_sessions(id,user_id,refresh_token_hash,expires_at,client_type) VALUES (?,?,?,CURRENT_TIMESTAMP+INTERVAL '1 hour','MOBILE')",session,id,UUID.randomUUID().toString());
  String email=db.queryForObject("SELECT email FROM users WHERE id=?",String.class,id);
  return new Actor(id,session,tokens.createAccessToken(users.findByEmailIgnoreCase(email).orElseThrow(),session));
 }
 static UUID review(JdbcTemplate db,UUID listener,UUID staff,boolean independent){
  db.update("INSERT INTO policy_documents(policy_key,version,title,summary,content,status,published_at) VALUES ('listener-code',900001,'Synthetic QA listener code','Isolated test boundary','Synthetic code reviewed only for disposable test fixtures','PUBLISHED',CURRENT_TIMESTAMP) ON CONFLICT DO NOTHING");
  db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='MODERATOR' ON CONFLICT DO NOTHING",staff);
  db.update("INSERT INTO listener_profiles(user_id,listening_style,languages,online) VALUES (?,'Reflective peer support',ARRAY['English','فارسی'],TRUE) ON CONFLICT(user_id) DO UPDATE SET status='ACTIVE'",listener);
  UUID app=UUID.randomUUID();db.update("INSERT INTO listener_applications(id,user_id,motivation,experience,weekly_availability,accepted_code,status,reviewed_by,reviewed_at) VALUES (?,?,'Synthetic approved motivation','Synthetic supervised experience','1 hour',TRUE,'APPROVED',?,CURRENT_TIMESTAMP)",app,listener,staff);
  db.update("INSERT INTO listener_training_progress(user_id,module_key) SELECT ?,module_key FROM listener_training_modules WHERE is_active ON CONFLICT DO NOTHING",listener);
  if(independent)db.update("INSERT INTO listener_live_authorizations(application_id,listener_id,reviewed_by,conduct_version) SELECT ?,?,?,MAX(version) FROM policy_documents WHERE policy_key='listener-code' AND status='PUBLISHED' AND published_at<=CURRENT_TIMESTAMP AND content IS NOT NULL",app,listener,staff);
  return app;
 }
 static void region(JdbcTemplate db){for(String service:new String[]{"COMMUNITY","LISTENER"})db.update("INSERT INTO regional_services(country_code,service,status) VALUES ('IR',?,'AVAILABLE') ON CONFLICT(country_code,service) DO UPDATE SET status='AVAILABLE'",service);}
 private PhaseIFixtures(){}
}
