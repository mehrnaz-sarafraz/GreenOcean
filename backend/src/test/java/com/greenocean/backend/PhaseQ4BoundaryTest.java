package com.greenocean.backend;

import com.greenocean.backend.trust.*;
import com.greenocean.backend.common.exception.ForbiddenException;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhaseQ4BoundaryTest {
    RecoveryReplay replay=mock(RecoveryReplay.class);
    RecoveryBoundary boundary=new RecoveryBoundary(replay);
    @AfterEach void clear(){SecurityContextHolder.clearContext();}
    boolean request(String method,String path){return boundary.preHandle(new MockHttpServletRequest(method,path),new MockHttpServletResponse(),new Object());}
    @Test void restoredPrivateReadsFailBeforeReplayButOperatorsCanAuthenticate(){
        when(replay.configured()).thenReturn(true);
        assertThatThrownBy(()->request("GET","/api/v1/wellbeing/journal")).isInstanceOf(ForbiddenException.class);
        assertThat(request("POST","/api/v1/auth/login")).isTrue();
        assertThat(request("GET","/api/v1/recovery/status")).isTrue();
        assertThat(request("GET","/api/v1/health")).isTrue();
    }
    @Test void subjectRestrictionStillDeniesReadsAfterGeneralRecovery(){
        UUID subject=UUID.randomUUID();when(replay.configured()).thenReturn(true);when(replay.ready()).thenReturn(true);
        when(replay.restricted(subject,"WELLBEING")).thenReturn(true);
        var jwt=Jwt.withTokenValue("synthetic-only").header("alg","none").subject(subject.toString()).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        assertThatThrownBy(()->request("GET","/api/v1/wellbeing/journal")).isInstanceOf(ForbiddenException.class);
    }
    @Test void pauseBlocksAdmissionWhilePreservingExistingRefundObligations(){
        when(replay.configured()).thenReturn(true);when(replay.ready()).thenReturn(true);when(replay.stopped("FINANCE")).thenReturn(true);
        assertThatThrownBy(()->request("POST","/api/v1/clinic/bookings")).isInstanceOf(ForbiddenException.class);
        assertThat(request("POST","/api/v1/clinic/appointments/00000000-0000-0000-0000-000000000001/refund")).isTrue();
        assertThat(request("GET","/api/v1/clinic/my-payments")).isTrue();
    }
    @Test void clinicalPauseAlsoStopsAppointmentAdmissionWithoutStoppingRevocationOrReports(){
        when(replay.configured()).thenReturn(true);when(replay.ready()).thenReturn(true);when(replay.stopped("CLINIC")).thenReturn(true);
        assertThatThrownBy(()->request("POST","/api/v1/clinic/appointments")).isInstanceOf(ForbiddenException.class);
        assertThat(request("POST","/api/v1/clinical/access-grants/00000000-0000-0000-0000-000000000001/revoke")).isTrue();
        assertThat(request("POST","/api/v1/clinic/appointments/00000000-0000-0000-0000-000000000001/transition")).isTrue();
    }
}
