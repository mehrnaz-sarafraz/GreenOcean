package com.greenocean.backend;
import com.greenocean.backend.wellbeing.service.*;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import org.springframework.test.context.ActiveProfiles;import org.springframework.transaction.annotation.Transactional;import org.junit.jupiter.api.Test;import java.util.*;import static org.assertj.core.api.Assertions.*;
@SpringBootTest(properties="greenocean.education.rewards-enabled=false") @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class PhaseO4DisabledTest extends PhaseO3Fixtures {
 @Autowired GrowthRewards rewards;
 @Test void disabledRewardsDoNotBlockLearningOrCreateUnapprovedCredit(){UUID id=create();publish(id);UUID key=(UUID)((Map<?,?>)((List<?>)learning.progress(learner.id(),id).get("lessons")).getFirst()).get("lesson_key");var opened=learning.open(learner.id(),id,key);learning.complete(learner.id(),id,key,new LearningCourses.Completion((String)opened.get("content_digest"),true));assertThat(db.queryForObject("SELECT count(*) FROM growth_reward_ledger WHERE owner_id=?",Integer.class,learner.id())).isZero();assertThatThrownBy(()->rewards.dashboard(learner.id(),0)).isInstanceOf(com.greenocean.backend.listener.live.LiveSupportException.class);}
}
