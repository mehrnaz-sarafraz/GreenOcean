package com.greenocean.backend;
import com.greenocean.backend.auth.config.*;
import com.greenocean.backend.auth.repository.UserSessionRepository;
import com.greenocean.backend.auth.service.SessionPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.*;
import java.time.Clock;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhaseQ4AuthenticationOutageTest {
    @Test void unavailableDatabaseCannotBeMistakenForSuccessfulOrExpiredAuthentication(){
        var db=mock(JdbcTemplate.class);
        when(db.queryForObject(anyString(),eq(Integer.class),any(),any())).thenThrow(new CannotGetJdbcConnectionException("Synthetic database outage"));
        var validator=new ActiveSessionJwtValidator(mock(UserSessionRepository.class),db,Clock.systemUTC(),mock(SessionPolicy.class));
        var token=Jwt.withTokenValue("synthetic-only").header("alg","none").subject(UUID.randomUUID().toString()).claim("sid",UUID.randomUUID().toString()).build();
        assertThat(validator.validate(token).getErrors()).extracting(OAuth2Error::getErrorCode).containsExactly("temporarily_unavailable");
    }
    @Test void temporaryVerificationFailureReturnsBoundedRetryWithoutPrivateDetails()throws Exception{
        var response=respond(List.of(new OAuth2Error("temporarily_unavailable")));
        assertThat(response.getStatus()).isEqualTo(503);assertThat(response.getHeader("Retry-After")).isEqualTo("5");
        assertThat(response.getContentAsString()).contains("temporarily unavailable").doesNotContain("Synthetic database failure","SELECT","jdbc:");
    }
    @Test void invalidOrExpiredTokenStillRequiresAuthenticationEvenDuringOutage()throws Exception{
        assertThat(respond(List.of(new OAuth2Error("invalid_token"))).getStatus()).isEqualTo(401);
        assertThat(respond(List.of(new OAuth2Error("invalid_token"),new OAuth2Error("temporarily_unavailable"))).getStatus()).isEqualTo(401);
    }
    MockHttpServletResponse respond(List<OAuth2Error> errors)throws Exception{
        var response=new MockHttpServletResponse();
        new RestAuthenticationEntryPoint(new tools.jackson.databind.ObjectMapper()).commence(
            new MockHttpServletRequest("GET","/api/v1/wellbeing/journal"),response,
            new AuthenticationServiceException("Synthetic database failure",new JwtValidationException("Synthetic database failure",errors)));
        return response;
    }
}
