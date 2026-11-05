package com.greenocean.backend;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseQ4FeedBudgetTest extends PhaseLFixtures {
    @Test void deepLegacyOffsetsAreRejectedBeforeExpensiveVisibilityScanning()throws Exception {
        var actor=member();
        mvc.perform(get("/api/v1/posts/feed").param("page","6").param("size","20").header("Authorization","Bearer "+actor.token())).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/posts/feed").param("page",Integer.toString(Integer.MAX_VALUE)).param("size","100").header("Authorization","Bearer "+actor.token())).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/posts/feed").param("page","0").param("size","100").header("Authorization","Bearer "+actor.token())).andExpect(status().isOk());
    }
    @Test void cursorDiscoveryPreservesPrivateFollowerBoundary()throws Exception {
        var author=member();var viewer=member();
        var visible=publish(author,"Synthetic public reflection for cursor authorization",true);
        var privateInput=payload("Synthetic followers-only reflection stays restricted",true);
        privateInput.put("visibility","FOLLOWERS");
        var restricted=submit("/api/v1/posts",author,privateInput,201);
        String result=read("/api/v1/posts/discovery?mode=DISCOVER&size=1",viewer).toString();
        assertThat(result).contains(visible.path("id").asText()).doesNotContain(restricted.path("id").asText(),"followers-only reflection");
    }
}
