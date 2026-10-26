package com.greenocean.backend;

import com.greenocean.backend.assistance.service.OutboxCipher;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseAIntegrationTest {
    @Autowired MockMvc mvc; @Autowired JdbcTemplate db; @Autowired ObjectMapper json; @Autowired OutboxCipher cipher;
    @Autowired jakarta.persistence.EntityManager entities;
    @Autowired org.springframework.transaction.PlatformTransactionManager manager;
    final String password="A long synthetic test password 🔑";
    @BeforeEach void rates() {var tx=new org.springframework.transaction.support.TransactionTemplate(manager);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);tx.executeWithoutResult(status->db.update("DELETE FROM auth_rate_windows"));}
    Map<String,Object> input() { return new HashMap<>(Map.of("email",UUID.randomUUID()+"@example.test","password",password,"countryCode","IR","adultSelfAttested",true,"termsVersion",1)); }
    ResultActions postJson(String path,Object body) throws Exception {return mvc.perform(post(path).contentType("application/json").content(json.writeValueAsString(body)));}
    String register(Map<String,Object> input) throws Exception {postJson("/api/v1/auth/register",input).andExpect(status().isCreated());return db.queryForObject("SELECT id::text FROM users WHERE lower(email)=lower(?)",String.class,input.get("email"));}
    JsonNode login(Map<String,Object> input) throws Exception {return json.readTree(postJson("/api/v1/auth/login",Map.of("email",input.get("email"),"password",input.get("password"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());}
    String code(Map<String,Object> input) {return cipher.decrypt(db.queryForObject("SELECT payload->>'encrypted' FROM email_outbox WHERE recipient=? AND template_key='email-verification' ORDER BY created_at DESC LIMIT 1",String.class,input.get("email")));}
    void verify(Map<String,Object> input) throws Exception {postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",input.get("email"),"code",code(input))).andExpect(status().isOk());entities.clear();}
    void enableCommunity() {db.update("INSERT INTO regional_services(country_code,service,status) VALUES ('IR','COMMUNITY','AVAILABLE') ON CONFLICT DO NOTHING");}
    @Test @Tag("happy") void minimalRegistrationHasPendingUserAndPrivateDefaults() throws Exception {
        var in=input();String id=register(in);
        assertThat(db.queryForObject("SELECT status FROM users WHERE id=?::uuid",String.class,id)).isEqualTo("PENDING");
        assertThat(db.queryForObject("SELECT age_method FROM account_eligibility WHERE user_id=?::uuid",String.class,id)).isEqualTo("SELF_ATTESTED");
        assertThat(db.queryForObject("SELECT is_profile_private FROM profiles WHERE user_id=?::uuid",Boolean.class,id)).isTrue();
        assertThat(db.queryForObject("SELECT birth_year FROM profiles WHERE user_id=?::uuid",Short.class,id)).isNull();
        assertThat(db.queryForObject("SELECT allow_message_requests FROM user_preferences WHERE user_id=?::uuid",Boolean.class,id)).isFalse();
        assertThat(db.queryForObject("SELECT COUNT(*) FROM policy_acceptances WHERE user_id=?::uuid",Integer.class,id)).isEqualTo(1);
        assertThat(db.queryForObject("SELECT r.name FROM roles r JOIN user_roles ur ON ur.role_id=r.id WHERE ur.user_id=?::uuid",String.class,id)).isEqualTo("USER");
    }
    @Test @Tag("happy") void verifyActivateLoginLogoutFlow() throws Exception {
        enableCommunity();var in=input();register(in);verify(in);var tokens=login(in);String token=tokens.get("accessToken").asText();
        mvc.perform(get("/api/v1/posts/feed").header("Authorization","Bearer "+token)).andExpect(status().isOk());
        postJson("/api/v1/auth/logout",Map.of("refreshToken",tokens.get("refreshToken").asText())).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/auth/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
    }
    @Test @Tag("happy") void publicResourcesAreSeparateAndAnonymous() throws Exception {
        mvc.perform(get("/api/v1/public/resources")).andExpect(status().isOk()).andExpect(jsonPath("$.audience").value("ALL_AGES")).andExpect(jsonPath("$.items.length()").value(2));
        mvc.perform(get("/api/v1/posts/feed")).andExpect(status().isUnauthorized());
    }
    @Test @Tag("happy") void ownerDeletionRevokesSessionAndHidesProfile() throws Exception {
        var in=input();String id=register(in);verify(in);var tokens=login(in);String token=tokens.get("accessToken").asText();
        mvc.perform(delete("/api/v1/account").header("Authorization","Bearer "+token).contentType("application/json").content(json.writeValueAsString(Map.of("password",password)))).andExpect(status().isNoContent());
        assertThat(db.queryForObject("SELECT status FROM account_deletions WHERE user_id=?::uuid",String.class,id)).isEqualTo("COMPLETED");
        assertThat(db.queryForObject("SELECT COUNT(*) FROM email_outbox WHERE recipient=?",Integer.class,in.get("email"))).isZero();
        mvc.perform(get("/api/v1/auth/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
        postJson("/api/v1/auth/refresh",Map.of("refreshToken",tokens.get("refreshToken").asText())).andExpect(status().isUnauthorized());
    }
    @Test @Tag("creative") void longUnicodePasswordAndNoDisplayDataAreSupported() throws Exception {
        var in=input();in.put("password","رمز عبور بسیار طولانی و امن برای آزمایش 🌊".repeat(2));register(in);login(in);
    }
    @Test @Tag("creative") void lowercaseCountryNormalizesAndNoTopicsAreRequired() throws Exception {var in=input();in.put("countryCode","ir");String id=register(in);assertThat(db.queryForObject("SELECT residence_country FROM account_eligibility WHERE user_id=?::uuid",String.class,id)).isEqualTo("IR");}
    @Test @Tag("creative") void sameCodeForDifferentEmailsHasDifferentProtectedHash() {assertThat(cipher.codeHash("a@example.test","123456")).isNotEqualTo(cipher.codeHash("b@example.test","123456"));}
    @Test @Tag("creative") void legacyAccountHasGraceButIsNeverAutoVerified() throws Exception {
        var in=input();String id=register(in);db.update("UPDATE users SET phase_a_enrolled=FALSE,status='ACTIVE' WHERE id=?::uuid",id);db.update("DELETE FROM account_eligibility WHERE user_id=?::uuid",id);entities.clear();
        String token=login(in).get("accessToken").asText();mvc.perform(get("/api/v1/posts/feed").header("Authorization","Bearer "+token)).andExpect(status().isOk());
        assertThat(db.queryForObject("SELECT email_verified FROM users WHERE id=?::uuid",Boolean.class,id)).isFalse();
    }
    @Test @Tag("confused") void malformedEmailAndWeakPasswordHaveFieldErrors() throws Exception {var in=input();in.put("email","wrong");in.put("password","short");postJson("/api/v1/auth/register",in).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors").isArray());}
    @Test @Tag("confused") void missingOrStaleTermsAreRejected() throws Exception {var in=input();in.remove("termsVersion");postJson("/api/v1/auth/register",in).andExpect(status().isBadRequest());in.put("termsVersion",999);postJson("/api/v1/auth/register",in).andExpect(status().isBadRequest());}
    @Test @Tag("confused") void expiredVerificationCanBeResent() throws Exception {
        var in=input();String id=register(in);String old=code(in);db.update("UPDATE email_verification_codes SET expires_at=CURRENT_TIMESTAMP-INTERVAL '1 minute' WHERE user_id=?::uuid",id);
        postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",old)).andExpect(status().isBadRequest());
        postJson("/api/v1/account-assistance/email-verification",Map.of("email",in.get("email"))).andExpect(status().isAccepted());verify(in);
    }
    @Test @Tag("confused") void unconfiguredCountryGivesAlternativeWithoutGrantingService() throws Exception {
        var in=input();register(in);verify(in);String token=login(in).get("accessToken").asText();
        mvc.perform(get("/api/v1/posts/feed").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/identity").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.services.COMMUNITY").value(false));
    }
    @Test @Tag("malicious") void minorCannotRegisterByDirectApi() throws Exception {var in=input();in.put("adultSelfAttested",false);postJson("/api/v1/auth/register",in).andExpect(status().isBadRequest());in.put("adultSelfAttested",true);in.put("birthYear",2020);postJson("/api/v1/auth/register",in).andExpect(status().isBadRequest());}
    @Test @Tag("malicious") void inventedCountryAndProfessionalRoleAreRejected() throws Exception {var in=input();in.put("countryCode","ZZ");postJson("/api/v1/auth/register",in).andExpect(status().isBadRequest());in.put("countryCode","IR");in.put("intendedRole","PROFESSIONAL");postJson("/api/v1/auth/register",in).andExpect(status().isBadRequest());}
    @Test @Tag("malicious") void pendingAccountCannotAccessSocialRoutes() throws Exception {
        enableCommunity();var in=input();register(in);String token=login(in).get("accessToken").asText();
        for(String path:List.of("/api/v1/posts/feed","/api/v1/communities","/api/v1/conversations","/api/v1/listeners")) mvc.perform(get(path).header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
    }
    @Test @Tag("malicious") void statusAndAgeMethodClaimsNeverGrantAuthority() throws Exception {
        var in=input();in.put("status","ACTIVE");in.put("roles",List.of("ADMIN"));in.put("ageMethod","INDEPENDENT");String id=register(in);
        assertThat(db.queryForObject("SELECT status FROM users WHERE id=?::uuid",String.class,id)).isEqualTo("PENDING");
        assertThat(db.queryForObject("SELECT age_method FROM account_eligibility WHERE user_id=?::uuid",String.class,id)).isEqualTo("SELF_ATTESTED");
    }
    @Test @Tag("malicious") void verificationIsSingleUseAndPayloadIsEncrypted() throws Exception {var in=input();register(in);String code=code(in);assertThat(db.queryForObject("SELECT payload::text FROM email_outbox WHERE recipient=?",String.class,in.get("email"))).doesNotContain(code);verify(in);postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",code)).andExpect(status().isBadRequest());}
    @Test @Tag("malicious") void guessingCodeIsBoundedAndFailureCounterSurvivesRollback() throws Exception {
        var in=input();register(in);String valid=code(in);
        for(int i=0;i<5;i++) postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code","xxxxxx")).andExpect(status().isBadRequest());
        // DTO failures are IP-limited; valid-format guesses consume the durable per-account limit.
        for(int i=0;i<5;i++) postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",valid.equals("000000")?"111111":"000000")).andExpect(status().isBadRequest());
        postJson("/api/v1/account-assistance/email-verification/confirm",Map.of("email",in.get("email"),"code",valid)).andExpect(status().isBadRequest());
    }
    @Test @Tag("malicious") void suspendedAccountCannotRefreshOrUseExistingAccess() throws Exception {
        var in=input();String id=register(in);verify(in);var tokens=login(in);db.update("UPDATE users SET status='SUSPENDED' WHERE id=?::uuid",id);entities.clear();
        postJson("/api/v1/auth/refresh",Map.of("refreshToken",tokens.get("refreshToken").asText())).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me").header("Authorization","Bearer "+tokens.get("accessToken").asText())).andExpect(status().isUnauthorized());
    }
    @Test @Tag("malicious") void cookieLoginRejectsMissingCsrfAndUntrustedOrigin() throws Exception {
        var in=input();register(in);
        postJson("/api/v1/auth/web/login",Map.of("email",in.get("email"),"password",password)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/auth/web/login").header("Origin","https://attacker.invalid").header("X-GreenOcean-CSRF","1").contentType("application/json").content(json.writeValueAsString(in))).andExpect(status().isForbidden());
    }
    @Test @Tag("happy") void browserSessionUsesHttpOnlyCookieAndCanRotate() throws Exception {
        var in=input();register(in);
        var result=mvc.perform(post("/api/v1/auth/web/login").header("Origin","http://localhost:8081").header("X-GreenOcean-CSRF","1")
            .contentType("application/json").content(json.writeValueAsString(Map.of("email",in.get("email"),"password",password))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.refreshToken").value("cookie")).andReturn();
        assertThat(result.getResponse().getHeader("Set-Cookie")).contains("HttpOnly","Secure","SameSite=Strict","Path=/api/v1/auth/web");
        String cookie=result.getResponse().getHeader("Set-Cookie").split(";",2)[0].substring("greenocean_refresh=".length());
        mvc.perform(post("/api/v1/auth/web/refresh").header("Origin","http://localhost:8081").header("X-GreenOcean-CSRF","1")
            .cookie(new jakarta.servlet.http.Cookie("greenocean_refresh",cookie))).andExpect(status().isOk());
    }
    @Test @Tag("happy") void passwordRecoveryInvalidatesEveryOldSession() throws Exception {
        var in=input();register(in);var old=login(in);
        postJson("/api/v1/account-assistance/password-reset",Map.of("email",in.get("email"))).andExpect(status().isAccepted());
        String reset=cipher.decrypt(db.queryForObject("SELECT payload->>'encrypted' FROM email_outbox WHERE recipient=? AND template_key='password-reset'",String.class,in.get("email")));
        postJson("/api/v1/account-assistance/password-reset/confirm",Map.of("token",reset,"newPassword","an entirely different long password")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/auth/me").header("Authorization","Bearer "+old.get("accessToken").asText())).andExpect(status().isUnauthorized());
        postJson("/api/v1/account-assistance/password-reset/confirm",Map.of("token",reset,"newPassword",password)).andExpect(status().isBadRequest());
    }
    @Test @Tag("happy") void fakeMailTransportDeliversAndPurgesEncryptedPayload() throws Exception {
        var in=input();register(in);String expected=code(in);var delivered=new ArrayList<String>();
        var worker=new com.greenocean.backend.assistance.service.EmailOutboxWorker(db,cipher,(id,to,template,secret)->delivered.add(secret),true,"unused@example.test");
        worker.deliver();assertThat(delivered).contains(expected);
        assertThat(db.queryForObject("SELECT payload::text FROM email_outbox WHERE recipient=?",String.class,in.get("email"))).isEqualTo("{}");
    }
    @Test @Tag("confused") void failedMailTransportIsRetryableWithoutExposingSecret() throws Exception {
        var in=input();register(in);
        var worker=new com.greenocean.backend.assistance.service.EmailOutboxWorker(db,cipher,(id,to,template,secret)->{throw new IllegalStateException();},true,"unused@example.test");
        worker.deliver();assertThat(db.queryForObject("SELECT status FROM email_outbox WHERE recipient=?",String.class,in.get("email"))).isEqualTo("FAILED");
        assertThat(db.queryForObject("SELECT attempts FROM email_outbox WHERE recipient=?",Integer.class,in.get("email"))).isEqualTo(1);
    }
    @Test @Tag("creative") void countryCatalogSearchSupportsLocalNamesAndNoImplicitServices() throws Exception {
        mvc.perform(get("/api/v1/geography/countries").param("q","Iran")).andExpect(status().isOk()).andExpect(jsonPath("$[0].code").value("IR"));
        mvc.perform(get("/api/v1/geography/countries").param("q","ایران")).andExpect(status().isOk()).andExpect(jsonPath("$[0].code").value("IR")).andExpect(jsonPath("$[0].local_names.fa").value("ایران"));
        mvc.perform(get("/api/v1/geography/services").param("country","IR")).andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("UNDER_REVIEW"));
        mvc.perform(get("/api/v1/geography/cities").param("country","IR")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }
    @Test @Tag("confused") void invalidCityCountryRelationshipCannotActivate() throws Exception {
        var in=input();register(in);var token=login(in).get("accessToken").asText();
        db.update("INSERT INTO geography_cities(geonames_id,country_code,name,catalog_version) VALUES (999000001,'US','Synthetic QA City','test-only')");
        mvc.perform(post("/api/v1/identity/activate").header("Authorization","Bearer "+token).contentType("application/json")
          .content(json.writeValueAsString(Map.of("adultSelfAttested",true,"residenceCountry","IR","termsVersion",1,"cityId",999000001)))).andExpect(status().isBadRequest());
    }
    @Test @Tag("malicious") void independentIneligibleDecisionCannotBecomeIndependentlyAdultThroughSelfClaim() throws Exception {
        var in=input();String id=register(in);db.update("UPDATE account_eligibility SET age_method='INDEPENDENT',age_result='INELIGIBLE' WHERE user_id=?::uuid",id);
        String token=login(in).get("accessToken").asText();
        mvc.perform(post("/api/v1/identity/activate").header("Authorization","Bearer "+token).contentType("application/json")
          .content(json.writeValueAsString(Map.of("adultSelfAttested",true,"residenceCountry","IR","termsVersion",1)))).andExpect(status().isForbidden());
    }
    @Test @Tag("malicious") void wrongPasswordCannotEraseAnyDataAndBodyUserIdCannotChangeOwner() throws Exception {
        var a=input();String id=register(a);var b=input();String otherId=register(b);String token=login(a).get("accessToken").asText();
        mvc.perform(delete("/api/v1/account").header("Authorization","Bearer "+token).contentType("application/json")
          .content(json.writeValueAsString(Map.of("password","wrong password","userId",otherId)))).andExpect(status().isForbidden());
        assertThat(db.queryForObject("SELECT COUNT(*) FROM users WHERE id IN (?::uuid,?::uuid) AND status<>'DELETED'",Integer.class,id,otherId)).isEqualTo(2);
        mvc.perform(delete("/api/v1/account").header("Authorization","Bearer "+token).contentType("application/json")
          .content(json.writeValueAsString(Map.of("password",password,"userId",otherId)))).andExpect(status().isNoContent());
        assertThat(db.queryForObject("SELECT status FROM users WHERE id=?::uuid",String.class,id)).isEqualTo("DELETED");
        assertThat(db.queryForObject("SELECT status FROM users WHERE id=?::uuid",String.class,otherId)).isEqualTo("PENDING");
    }
    @Test @Tag("malicious") void removedRoleInvalidatesExistingJwt() throws Exception {
        var in=input();String id=register(in);String token=login(in).get("accessToken").asText();db.update("DELETE FROM user_roles WHERE user_id=?::uuid",id);
        mvc.perform(get("/api/v1/auth/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
    }
    @Test @Tag("happy") void professionalApplicantDoesNotReceiveProfessionalAuthority() throws Exception {
        enableCommunity();var in=input();String id=register(in);verify(in);String token=login(in).get("accessToken").asText();
        var application=new HashMap<String,Object>();application.put("professionalType","PSYCHOLOGIST");application.put("title","Synthetic applicant");
        application.put("specialization","Synthetic support");application.put("bio","Synthetic application for isolated QA only");application.put("yearsOfExperience",2);
        application.put("country","IR");application.put("licenseNumber","SYNTHETIC-QA-ONLY");application.put("documentNames",List.of("synthetic.pdf"));
        application.put("specialties",List.of());application.put("languages",List.of("English"));application.put("education",List.of("Synthetic qualification"));application.put("consultationModes",List.of("ONLINE"));
        mvc.perform(post("/api/v1/professional-applications").header("Authorization","Bearer "+token).contentType("application/json").content(json.writeValueAsString(application)))
          .andExpect(status().isCreated());
        assertThat(db.queryForObject("SELECT verification_status FROM professional_profiles WHERE user_id=?::uuid",String.class,id)).isEqualTo("PENDING");
        assertThat(db.queryForObject("SELECT COUNT(*) FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=?::uuid AND r.name='PROFESSIONAL'",Integer.class,id)).isZero();
        mvc.perform(get("/api/v1/professional/availability").header("Authorization","Bearer "+token)).andExpect(status().isNotFound());
    }
    @Test @Tag("malicious") void verifiedProfessionalStillNeedsExplicitUnexpiredCareScope() throws Exception {
        var requester=input();String userId=register(requester);verify(requester);var provider=input();String providerId=register(provider);verify(provider);
        db.update("INSERT INTO professional_profiles(user_id,professional_type,title,verification_status,verified_at) VALUES (?::uuid,'PSYCHOLOGIST','Synthetic professional','VERIFIED',CURRENT_TIMESTAMP)",providerId);
        db.update("INSERT INTO regional_services(country_code,service,status) VALUES ('IR','PROFESSIONAL_CARE','AVAILABLE')");
        var access=new com.greenocean.backend.identity.AccessPolicy(db,java.time.Instant.MAX);
        assertThatThrownBy(()->access.requireCare(UUID.fromString(userId),UUID.fromString(providerId))).isInstanceOf(com.greenocean.backend.common.exception.ForbiddenException.class);
        db.update("INSERT INTO professional_care_regions(professional_user_id,country_code,approved_by,expires_at) VALUES (?::uuid,'IR',?::uuid,CURRENT_TIMESTAMP+INTERVAL '1 day')",providerId,providerId);
        assertThatCode(()->access.requireCare(UUID.fromString(userId),UUID.fromString(providerId))).doesNotThrowAnyException();
        db.update("UPDATE professional_care_regions SET approved_at=CURRENT_TIMESTAMP-INTERVAL '2 days',expires_at=CURRENT_TIMESTAMP-INTERVAL '1 day' WHERE professional_user_id=?::uuid",providerId);
        assertThatThrownBy(()->access.requireCare(UUID.fromString(userId),UUID.fromString(providerId))).isInstanceOf(com.greenocean.backend.common.exception.ForbiddenException.class);
    }
}
