package com.greenocean.backend;
import com.greenocean.backend.auth.service.*;
import com.greenocean.backend.auth.dto.*;
import com.greenocean.backend.common.exception.UnauthorizedException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test")
class PhaseAConcurrencyTest {
    @Autowired RegistrationService registration;@Autowired AuthenticationService auth;@Autowired JdbcTemplate db;
    String email;
    TokenResponse session() {
        email="concurrency-"+UUID.randomUUID()+"@example.test";
        registration.register(new RegisterRequest(email,"a long synthetic concurrency password",null,null,null,"IR",null,null,true,1));
        return auth.login(new LoginRequest(email,"a long synthetic concurrency password",null));
    }
    @AfterEach void clean() {
        if(email!=null) {db.update("DELETE FROM email_outbox WHERE recipient=?",email);db.update("DELETE FROM users WHERE email=?",email);}
    }
    @Test @Tag("malicious") void retiredRefreshReplayRevokesTheWholeSessionFamily() {
        var old=session();var fresh=auth.refresh(old.refreshToken());
        assertThatThrownBy(()->auth.refresh(old.refreshToken())).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(()->auth.refresh(fresh.refreshToken())).isInstanceOf(UnauthorizedException.class);
    }
    @Test @Tag("creative") void simultaneousRefreshHasOneWinnerAndNoTwoUsableTokens() throws Exception {
        var old=session();var start=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(2)) {
            var first=pool.submit(()->attempt(old.refreshToken(),start));var second=pool.submit(()->attempt(old.refreshToken(),start));start.countDown();
            List<String> results=Arrays.asList(first.get(10,TimeUnit.SECONDS),second.get(10,TimeUnit.SECONDS));
            assertThat(results.stream().filter(Objects::nonNull).count()).isEqualTo(1);
            for(String token:results) if(token!=null) assertThatThrownBy(()->auth.refresh(token)).isInstanceOf(UnauthorizedException.class);
        }
    }
    private String attempt(String token,CountDownLatch start) throws Exception {
        start.await(5,TimeUnit.SECONDS);try {return auth.refresh(token).refreshToken();}catch(UnauthorizedException e) {return null;}
    }
}
