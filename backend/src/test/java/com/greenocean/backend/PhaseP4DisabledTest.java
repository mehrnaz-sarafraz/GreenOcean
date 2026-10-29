package com.greenocean.backend;
import com.greenocean.backend.professional.service.ProfessionalReputation;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import org.springframework.test.context.ActiveProfiles;import org.springframework.transaction.annotation.Transactional;import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseP4DisabledTest extends PhaseO3Fixtures {
 @Autowired ProfessionalReputation reputation;
 @Test void publicationAndSensitiveFeedbackStayDefaultOffDespiteAdminRole(){assertThatThrownBy(()->reputation.rankings(issuer.id(),"PSYCHOLOGIST","TEXT")).isInstanceOf(com.greenocean.backend.common.exception.ForbiddenException.class);}
}
