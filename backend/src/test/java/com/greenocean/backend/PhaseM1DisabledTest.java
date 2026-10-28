package com.greenocean.backend;
import org.junit.jupiter.api.*;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import org.springframework.test.context.ActiveProfiles;import org.springframework.transaction.annotation.Transactional;import java.util.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseM1DisabledTest extends PhaseLFixtures {
 @Test void intakeIsNotSilentlyActivated()throws Exception{var owner=member();submit("/api/v1/community-proposals",owner,Map.of("name","Synthetic proposal","description","A safe peer circle","purpose","Share experiences","privacyType","PUBLIC","rules",List.of("Respect privacy"),"revision",0),403);}
}
