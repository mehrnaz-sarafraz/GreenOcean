package com.greenocean.backend;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
@SpringBootTest(properties="greenocean.social.publishing-enabled=false") @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseL1DisabledTest extends PhaseLFixtures {
 @Test void unapprovedOperationsCannotPublish()throws Exception {
  var a=member();submit("/api/v1/posts",a,payload("Publishing must stay disabled",true),403);
 }
}
