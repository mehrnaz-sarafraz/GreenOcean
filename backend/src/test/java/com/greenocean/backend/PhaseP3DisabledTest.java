package com.greenocean.backend;
import com.greenocean.backend.professional.service.ClinicalCare;import com.greenocean.backend.common.exception.ForbiddenException;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import org.springframework.test.context.ActiveProfiles;import org.springframework.transaction.annotation.Transactional;import org.junit.jupiter.api.Test;import java.util.UUID;import static org.assertj.core.api.Assertions.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseP3DisabledTest extends PhaseO3Fixtures {
 @Autowired ClinicalCare care;
 @Test void defaultOffClinicalProcessingCannotBeEnabledByAnAdminRoleOrConsentPayload(){assertThatThrownBy(()->care.notices(issuer.id(),issuer.session(),UUID.randomUUID(),"en")).isInstanceOf(ForbiddenException.class);}
}
