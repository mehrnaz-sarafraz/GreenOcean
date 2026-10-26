package com.greenocean.backend;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class PhaseCConcurrencyTest {
    @Autowired MockMvc mvc; @Autowired JdbcTemplate db; @Autowired ObjectMapper json;
    String email;
    @AfterEach void cleanSyntheticAccount() {
        if(email!=null) {
            db.update("DELETE FROM email_outbox WHERE recipient=?",email);
            db.update("DELETE FROM users WHERE email=?",email);
        }
    }
    @Test @Tag("malicious") void simultaneousCaseVariantsHaveOneAccountAndIdenticalPublicResponses() throws Exception {
        email="parallel-"+UUID.randomUUID()+"@example.test";
        var barrier=new CountDownLatch(1);
        db.update("DELETE FROM auth_rate_windows");
        try(var pool=Executors.newFixedThreadPool(2)) {
            var a=pool.submit(()->attempt(email,barrier));
            var b=pool.submit(()->attempt(email.toUpperCase(Locale.ROOT),barrier));
            barrier.countDown();
            assertThat(a.get(30,TimeUnit.SECONDS)).isEqualTo(b.get(30,TimeUnit.SECONDS));
        }
        assertThat(db.queryForObject("SELECT count(*) FROM users WHERE lower(email)=lower(?)",Integer.class,email)).isEqualTo(1);
        assertThat(db.queryForObject("SELECT count(*) FROM profiles WHERE user_id=(SELECT id FROM users WHERE email=?)",Integer.class,email)).isEqualTo(1);
        assertThat(db.queryForObject("SELECT count(*) FROM policy_acceptances WHERE user_id=(SELECT id FROM users WHERE email=?)",Integer.class,email)).isEqualTo(1);
        assertThat(db.queryForObject("SELECT count(*) FROM email_outbox WHERE recipient=?",Integer.class,email)).isEqualTo(1);
    }
    String attempt(String address,CountDownLatch barrier) throws Exception {
        barrier.await(5,TimeUnit.SECONDS);
        var result=mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(json.writeValueAsString(Map.of("email",address,"password","A synthetic concurrent signup phrase","countryCode","IR","adultSelfAttested",true,"termsVersion",1)))).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(201);
        return result.getContentAsString();
    }
}
