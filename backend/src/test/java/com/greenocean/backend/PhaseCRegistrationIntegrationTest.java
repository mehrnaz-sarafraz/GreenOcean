package com.greenocean.backend;

import com.greenocean.backend.assistance.service.OutboxCipher;
import com.greenocean.backend.auth.config.CompatiblePasswordEncoder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseCRegistrationIntegrationTest {
    @Autowired MockMvc mvc; @Autowired JdbcTemplate db; @Autowired ObjectMapper json; @Autowired OutboxCipher cipher;
    @Autowired jakarta.persistence.EntityManager entities;
    @Autowired org.springframework.transaction.PlatformTransactionManager manager;
    final String password="Synthetic registration phrase 🌊 with spaces";
    @BeforeEach void resetRates() {
        var tx=new org.springframework.transaction.support.TransactionTemplate(manager);
        tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.executeWithoutResult(s->db.update("DELETE FROM auth_rate_windows"));
    }
    Map<String,Object> input() {return new HashMap<>(Map.of("email",UUID.randomUUID()+"@example.test","password",password,"countryCode","IR","adultSelfAttested",true,"termsVersion",1));}
    ResultActions postJson(String path,Object input) throws Exception {return mvc.perform(post(path).contentType("application/json").content(json.writeValueAsString(input)));}
    String register(Map<String,Object> in) throws Exception {
        postJson("/api/v1/auth/register",in).andExpect(status().isCreated()).andExpect(jsonPath("$.message").isString()).andExpect(jsonPath("$.userId").doesNotExist());
        return db.queryForObject("SELECT id::text FROM users WHERE lower(email)=lower(?)",String.class,in.get("email").toString().trim());
    }
    String code(Map<String,Object> in) {return cipher.decrypt(db.queryForObject("SELECT payload->>'encrypted' FROM email_outbox WHERE recipient=? AND template_key='email-verification'",String.class,in.get("email").toString().trim().toLowerCase(Locale.ROOT)));}
    void verify(Map<String,Object> in,String code) throws Exception {postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email").toString().trim(),"code",code)).andExpect(status().isOk());entities.clear();}
    @Test @Tag("happy") void minimalRegistrationCreatesOnePrivatePendingAccountWithEvidence() throws Exception {
        var in=input();String id=register(in);
        assertThat(db.queryForMap("SELECT status,email_verified FROM users WHERE id=?::uuid",id)).containsEntry("status","PENDING").containsEntry("email_verified",false);
        var profile=db.queryForMap("SELECT username,display_name,city,birth_year,is_profile_private,show_location,show_birth_year FROM profiles WHERE user_id=?::uuid",id);
        assertThat(profile).containsEntry("display_name","New member").containsEntry("city",null).containsEntry("birth_year",null).containsEntry("is_profile_private",true).containsEntry("show_location",false).containsEntry("show_birth_year",false);
        assertThat(profile.get("username").toString()).startsWith("member_").doesNotContain(in.get("email").toString().split("@")[0]);
        assertThat(db.queryForObject("SELECT count(*) FROM policy_acceptances WHERE user_id=?::uuid AND policy_key='terms' AND version=1 AND accepted_at<=CURRENT_TIMESTAMP",Integer.class,id)).isEqualTo(1);
        assertThat(db.queryForObject("SELECT allow_message_requests FROM user_preferences WHERE user_id=?::uuid",Boolean.class,id)).isFalse();
        assertThat(db.queryForObject("SELECT password_hash FROM users WHERE id=?::uuid",String.class,id)).startsWith("{pbkdf2}").doesNotContain(password);
    }
    @Test @Tag("creative") void trimCasePlusAndDotsFollowTheIdentityContract() throws Exception {
        var in=input();String local="Case."+UUID.randomUUID().toString().substring(0,8)+"+tag";in.put("email","  "+local+"@EXAMPLE.TEST  ");String id=register(in);
        assertThat(db.queryForObject("SELECT email FROM users WHERE id=?::uuid",String.class,id)).isEqualTo(local.toLowerCase(Locale.ROOT)+"@example.test");
    }
    @Test @Tag("creative") void maximumCodePointPasswordWorksThroughDtoAndLogin() throws Exception {
        var in=input();in.put("password","🌊🌍🌱🔑".repeat(32));register(in);
        postJson("/api/v1/auth/login",Map.of("email",in.get("email"),"password",in.get("password"))).andExpect(status().isOk());
    }
    @Test @Tag("confused") void duplicateAndNewResponsesMatchWithoutChangingCredentialsOrSendingMail() throws Exception {
        var in=input();String first=postJson("/api/v1/auth/register",in).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String hash=db.queryForObject("SELECT password_hash FROM users WHERE email=?",String.class,in.get("email"));
        String originalCode=code(in);in.put("password","a different attempted registration password");in.put("email",in.get("email").toString().toUpperCase(Locale.ROOT));
        String second=postJson("/api/v1/auth/register",in).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        assertThat(second).isEqualTo(first);
        assertThat(db.queryForObject("SELECT count(*) FROM users WHERE lower(email)=lower(?)",Integer.class,in.get("email"))).isEqualTo(1);
        assertThat(db.queryForObject("SELECT password_hash FROM users WHERE lower(email)=lower(?)",String.class,in.get("email"))).isEqualTo(hash);
        assertThat(code(in)).isEqualTo(originalCode);
    }
    @Test @Tag("confused") void weakPasswordAndStaleTermsHaveSpecificSafeFieldsAndNoMutation() throws Exception {
        var in=input();in.put("password","passwordpassword");
        postJson("/api/v1/auth/register",in).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors[0].field").value("password"));
        in.put("password",password);in.put("termsVersion",999);
        postJson("/api/v1/auth/register",in).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors[0].field").value("termsVersion"));
        assertThat(db.queryForObject("SELECT count(*) FROM users WHERE email=?",Integer.class,in.get("email"))).isZero();
    }
    @Test @Tag("confused") void updatedTermsRequireAcceptanceBeforePendingActivation() throws Exception {
        var in=input();String id=register(in);String code=code(in);
        db.update("INSERT INTO policy_documents(policy_key,version,title,summary,content,status,published_at) SELECT policy_key,2,title,summary,content,'PUBLISHED',CURRENT_TIMESTAMP FROM policy_documents WHERE policy_key='terms' AND version=1");
        verify(in,code);
        assertThat(db.queryForObject("SELECT status FROM users WHERE id=?::uuid",String.class,id)).isEqualTo("PENDING");
        var tokens=json.readTree(postJson("/api/v1/auth/login",Map.of("email",in.get("email"),"password",password)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        mvc.perform(post("/api/v1/identity/activate").header("Authorization","Bearer "+tokens.get("accessToken").asText()).contentType("application/json").content(json.writeValueAsString(Map.of("adultSelfAttested",true,"residenceCountry","IR","termsVersion",2)))).andExpect(status().isOk());
        assertThat(db.queryForObject("SELECT status FROM users WHERE id=?::uuid",String.class,id)).isEqualTo("ACTIVE");
    }
    @Test @Tag("malicious") void extraAuthorityClaimsDoNotGrantPrivilegesOrExposeOptionalData() throws Exception {
        var in=input();in.put("roles",List.of("ADMIN","PROFESSIONAL","LISTENER"));in.put("isAdmin",true);in.put("ageMethod","INDEPENDENT");in.put("status","ACTIVE");in.put("displayName","<script>alert(1)</script>");String id=register(in);
        assertThat(db.queryForList("SELECT r.name FROM roles r JOIN user_roles ur ON ur.role_id=r.id WHERE ur.user_id=?::uuid",String.class,id)).containsExactly("USER");
        assertThat(db.queryForObject("SELECT age_method FROM account_eligibility WHERE user_id=?::uuid",String.class,id)).isEqualTo("SELF_ATTESTED");
        assertThat(db.queryForObject("SELECT display_name FROM profiles WHERE user_id=?::uuid",String.class,id)).isEqualTo("New member");
    }
    @Test @Tag("malicious") void rejectedEligibilityAndRoleRequestsCreateNoAccounts() throws Exception {
        for(var bad:List.of(Map.of("adultSelfAttested",false),Map.of("countryCode","ZZ"),Map.of("intendedRole","ADMIN"),Map.of("termsVersion",999),Map.of("email","x' OR 1=1;--"))) {
            var in=input();in.putAll(bad);int before=db.queryForObject("SELECT count(*) FROM users",Integer.class);
            postJson("/api/v1/auth/register",in).andExpect(status().isBadRequest());
            assertThat(db.queryForObject("SELECT count(*) FROM users",Integer.class)).isEqualTo(before);
        }
    }
    @Test @Tag("malicious") void malformedAndOversizedBodiesAreSafeAndDoNotMutate() throws Exception {
        int before=db.queryForObject("SELECT count(*) FROM users",Integer.class);
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content("{oops")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content("x".repeat(8193))).andExpect(status().isPayloadTooLarge());
        assertThat(db.queryForObject("SELECT count(*) FROM users",Integer.class)).isEqualTo(before);
    }
    @Test @Tag("malicious") void anotherAccountsCodeCannotVerifyTheVictim() throws Exception {
        var a=input();var b=input();String victim=register(a);register(b);
        String wrong=code(b);if(wrong.equals(code(a)))wrong=wrong.equals("000000")?"111111":"000000";
        postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",a.get("email"),"code",wrong)).andExpect(status().isBadRequest());
        assertThat(db.queryForObject("SELECT email_verified FROM users WHERE id=?::uuid",Boolean.class,victim)).isFalse();
    }
    @Test @Tag("happy") void activationStillRequiresRegionalPermission() throws Exception {
        var in=input();String id=register(in);verify(in,code(in));
        assertThat(db.queryForObject("SELECT status FROM users WHERE id=?::uuid",String.class,id)).isEqualTo("ACTIVE");
        String token=json.readTree(postJson("/api/v1/auth/login",Map.of("email",in.get("email"),"password",password)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("accessToken").asText();
        mvc.perform(get("/api/v1/posts/feed").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
    }
    @Test @Tag("malicious") void registrationHasDurableRateLimitWithRetryAndNoMutation() throws Exception {
        for(int i=0;i<30;i++)mvc.perform(post("/api/v1/auth/register").contentType("application/json").content("{}")).andExpect(status().isBadRequest());
        var in=input();postJson("/api/v1/auth/register",in).andExpect(status().isTooManyRequests()).andExpect(header().string("Retry-After","60"));
        assertThat(db.queryForObject("SELECT count(*) FROM users WHERE email=?",Integer.class,in.get("email"))).isZero();
    }
}
