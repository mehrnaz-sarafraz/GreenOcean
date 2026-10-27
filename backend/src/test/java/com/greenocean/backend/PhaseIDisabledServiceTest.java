package com.greenocean.backend;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseIDisabledServiceTest {
 @Autowired MockMvc mvc;@Autowired JdbcTemplate db;@Autowired UserRepository users;@Autowired TokenService tokens;
 @Test void productionDefaultDoesNotEnableLiveHumansOrBlockPublicResources()throws Exception{
  PhaseIFixtures.region(db);var user=PhaseIFixtures.member(db,users,tokens);String auth="Bearer "+user.token();
  mvc.perform(get("/api/v1/listener-support/availability").header("Authorization",auth)).andExpect(status().isOk()).andExpect(jsonPath("$.enabled").value(false)).andExpect(jsonPath("$.listeners").isEmpty());
  mvc.perform(post("/api/v1/listener-support/requests").header("Authorization",auth).contentType("application/json").content("{\"clientRequestId\":\""+UUID.randomUUID()+"\"}")).andExpect(status().isServiceUnavailable());
  mvc.perform(get("/api/v1/crisis-resources")).andExpect(status().isOk());
 }
}
