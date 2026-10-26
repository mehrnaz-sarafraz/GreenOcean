package com.greenocean.backend;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseBPublicAccessIntegrationTest {
    @Autowired MockMvc mvc;@Autowired JdbcTemplate db;@Autowired PlatformTransactionManager manager;
    @BeforeEach void independentBudgetReset(){var tx=new TransactionTemplate(manager);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);tx.executeWithoutResult(s->db.update("DELETE FROM auth_rate_windows"));}
    @Test @Tag("happy") void publishedPoliciesArePublicAndVersioned() throws Exception {
        for(String key:new String[]{"terms","privacy","community-guidelines"})mvc.perform(get("/api/v1/policies/"+key)).andExpect(status().isOk()).andExpect(jsonPath("$.version").isNumber()).andExpect(jsonPath("$.content").isNotEmpty()).andExpect(jsonPath("$.publishedAt").exists());
    }
    @Test @Tag("happy") void urgentResourcesAndCuratedEducationNeedNoToken() throws Exception {
        mvc.perform(get("/api/v1/crisis-resources")).andExpect(status().isOk()).andExpect(jsonPath("$[0].sourceUrl").exists());
        mvc.perform(get("/api/v1/public/resources")).andExpect(status().isOk()).andExpect(jsonPath("$.audience").value("ALL_AGES"));
    }
    @Test @Tag("happy") void countryAndServiceCatalogsExposeOnlyPublicConfiguration() throws Exception {
        String body=mvc.perform(get("/api/v1/geography/countries").param("q","Iran")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(body).contains("ایران").doesNotContain("email","user_id","password","supportTopics");
        mvc.perform(get("/api/v1/geography/services").param("country","IR")).andExpect(status().isOk());
    }
    @Test @Tag("creative") void localLanguageQueryDoesNotRequireAnAccount() throws Exception {
        mvc.perform(get("/api/v1/geography/countries").param("q","ایران").param("locale","fa")).andExpect(status().isOk()).andExpect(jsonPath("$[0].code").value("IR"));
    }
    @Test @Tag("creative") void latestPublishedVersionWinsOverANewerDraft() throws Exception {
        policy("qa-version","PUBLISHED",1,"CURRENT_TIMESTAMP-INTERVAL '1 day'","Published QA text");
        policy("qa-version","DRAFT",2,"NULL","Private QA draft");
        mvc.perform(get("/api/v1/policies/qa-version")).andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1)).andExpect(jsonPath("$.content").value("Published QA text"));
    }
    @Test @Tag("creative") void caseInsensitivePolicyKeyStillUsesThePublishedContract() throws Exception {
        mvc.perform(get("/api/v1/policies/TERMS")).andExpect(status().isOk()).andExpect(jsonPath("$.key").value("terms"));
    }
    @Test @Tag("confused") void unknownOrRemovedPublicDocumentsFailClearly() throws Exception {
        mvc.perform(get("/api/v1/policies/nonexistent-qa-document")).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Published policy was not found"));
        mvc.perform(get("/api/v1/public/resources/nonexistent-qa-resource")).andExpect(status().isNotFound());
    }
    @Test @Tag("confused") void malformedOrOversizedQueriesAreRejected() throws Exception {
        mvc.perform(get("/api/v1/geography/countries").param("q","a".repeat(101))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/geography/cities").param("country","ZZ")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/geography/cities").param("country","IR").param("subdivision","x".repeat(17))).andExpect(status().isBadRequest());
    }
    @Test @Tag("confused") void inactiveAndFutureReviewedCrisisEntriesAreNotPresentedAsCurrent() throws Exception {
        db.update("INSERT INTO crisis_resources(country_code,region_name,title,detail,action_url,source_url,last_verified_on,is_active) VALUES ('ZZ','Synthetic QA','Unreviewed QA','Not current','https://example.test/qa','https://example.test/official',CURRENT_DATE+1,TRUE),('ZZ','Synthetic QA','Inactive QA','Not current','https://example.test/qa','https://example.test/official',CURRENT_DATE,FALSE)");
        String body=mvc.perform(get("/api/v1/crisis-resources")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();assertThat(body).doesNotContain("Unreviewed QA","Inactive QA");
    }
    @Test @Tag("malicious") void privateEndpointsRemainUnavailableEvenWithLocaleAndCountryParameters() throws Exception {
        for(String path:new String[]{"posts/feed","posts/00000000-0000-0000-0000-000000000001","articles","media-recommendations","profiles/qa-private","conversations","preferences/me","profiles/me","listeners","professionals","communities","admin/operations"})
            mvc.perform(get("/api/v1/"+path).param("locale","fa").param("country","IR")).andExpect(status().isUnauthorized());
    }
    @Test @Tag("malicious") void draftsAndFuturePublicationAreNeverLeaked() throws Exception {
        policy("qa-private","DRAFT",1,"NULL","Private unreleased QA");policy("qa-future","PUBLISHED",1,"CURRENT_TIMESTAMP+INTERVAL '1 day'","Future QA");
        mvc.perform(get("/api/v1/policies/qa-private")).andExpect(status().isNotFound());mvc.perform(get("/api/v1/policies/qa-future")).andExpect(status().isNotFound());
    }
    @Test @Tag("malicious") void resourceIdentifiersAndExtraParametersCannotRetrieveMemberData() throws Exception {
        String body=mvc.perform(get("/api/v1/public/resources").param("userId","00000000-0000-0000-0000-000000000001").param("includePrivate","true")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("email","passwordHash","refreshToken","supportTopics");
        mvc.perform(get("/api/v1/public/resources/00000000-0000-0000-0000-000000000001")).andExpect(status().isNotFound());
    }
    @Test @Tag("malicious") void excessPublicTrafficUsesOneBudgetAcrossRoutesAndQueryVariants() throws Exception {
        for(int i=0;i<120;i++)mvc.perform(get("/api/v1/geography/countries").param("q","no-qa-match-"+i)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/public/resources").param("locale","fa")).andExpect(status().isTooManyRequests()).andExpect(header().string("Retry-After","60"));
    }
    @Test @Tag("malicious") void unsafePolicyKeysAreNotNormalizedIntoValidDocuments() throws Exception {
        mvc.perform(get("/api/v1/policies/terms<script>")).andExpect(status().isBadRequest());
    }
    private void policy(String key,String state,int version,String published,String content){
        // `published` is a constant expression supplied only by this synthetic test fixture.
        db.update("INSERT INTO policy_documents(policy_key,version,title,summary,content,status,published_at) VALUES (?,?,'QA document','Synthetic fixture',?,?,"+published+")",key,version,content,state);
    }
}
