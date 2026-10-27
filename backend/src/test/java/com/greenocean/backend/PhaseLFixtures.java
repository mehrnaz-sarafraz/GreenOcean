package com.greenocean.backend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import tools.jackson.databind.*;
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
abstract class PhaseLFixtures {
 @Autowired MockMvc mvc; @Autowired JdbcTemplate db; @Autowired ObjectMapper json;
 @Autowired jakarta.persistence.EntityManager entities;
 record Member(UUID id,String token,String email){}
 Member member() throws Exception {
  String email="l-"+UUID.randomUUID()+"@example.test";
  mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("email",email,"password","Synthetic L passphrase 2026","countryCode","IR","adultSelfAttested",true,"termsVersion",1)))).andExpect(status().isCreated());
  PhaseAFixtures.activate(db,email);entities.clear();
  var result=mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("email",email,"password","Synthetic L passphrase 2026")))).andExpect(status().isOk()).andReturn();
  return new Member(db.queryForObject("SELECT id FROM users WHERE email=?",UUID.class,email),json.readTree(result.getResponse().getContentAsString()).path("accessToken").asText(),email);
 }
 Map<String,Object> payload(String text,boolean anonymous){return new HashMap<>(Map.of("body",text,"anonymous",anonymous,"publicationIdentity",anonymous?"ANONYMOUS":"SOCIAL_IDENTITY","visibility","PUBLIC","broaderAudienceConfirmed",true));}
 JsonNode publish(Member m,String text,boolean anonymous)throws Exception{return submit("/api/v1/posts",m,payload(text,anonymous),201);}
 JsonNode submit(String path,Member m,Object data,int status)throws Exception {
  var r=mvc.perform(post(path).header("Authorization","Bearer "+m.token).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(data))).andExpect(status().is(status)).andReturn();
  return json.readTree(r.getResponse().getContentAsString());
 }
 JsonNode read(String path,Member m)throws Exception{return json.readTree(mvc.perform(get(path).header("Authorization","Bearer "+m.token)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());}
}
