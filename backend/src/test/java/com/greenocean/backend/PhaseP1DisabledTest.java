package com.greenocean.backend;
import com.greenocean.backend.professional.service.*;import com.greenocean.backend.identity.AccessPolicy;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import org.springframework.test.context.ActiveProfiles;import org.springframework.transaction.annotation.Transactional;import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.*;
@SpringBootTest(properties="greenocean.clinic.enabled=false") @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseP1DisabledTest extends PhaseO3Fixtures {
 @Autowired ProfessionalAuthority authority;@Autowired AccessPolicy access;
 @Test void evenSyntheticCompleteEvidenceCannotBypassDisabledClinicalRelease(){PhasePFixtures.qualified(db,learner.id(),reviewer.id());assertThat(db.queryForObject("SELECT professional_scope_live(?,'IR','TEXT')",Boolean.class,learner.id())).isTrue();assertThatThrownBy(()->authority.offer(learner.id(),"IR","TEXT")).isInstanceOf(com.greenocean.backend.common.exception.ForbiddenException.class);assertThatThrownBy(()->access.requireCare(other.id(),learner.id())).isInstanceOf(com.greenocean.backend.common.exception.ForbiddenException.class);}
}
