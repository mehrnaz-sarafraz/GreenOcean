package com.greenocean.backend;
import org.junit.jupiter.api.*;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;import org.springframework.transaction.annotation.Transactional;import org.springframework.jdbc.core.JdbcTemplate;
import com.greenocean.backend.preference.service.*;import java.util.*;import static org.assertj.core.api.Assertions.*;
@SpringBootTest @ActiveProfiles("test") @Transactional
class PhaseJDisabledGovernanceTest {
 @Autowired JdbcTemplate db;@Autowired PrivacyGovernanceService privacy;@Autowired PolicyPublicationService publication;
 UUID owner(){return db.queryForObject("INSERT INTO users(email,password_hash,status) VALUES (?,'synthetic','ACTIVE') RETURNING id",UUID.class,"j-disabled-"+UUID.randomUUID()+"@example.test");}
 @Test @Tag("malicious") void defaultConfigurationDoesNotEnableProcessingOrHumanIntake(){UUID user=owner();assertThat(privacy.snapshot(user).get("manualIntakeAvailable")).isEqualTo(false);assertThat(privacy.approvedNotice("SUPPORT_TOPICS")).isEmpty();assertThatThrownBy(()->privacy.requestRight(user,"ACCESS",UUID.randomUUID())).isInstanceOf(com.greenocean.backend.common.exception.ForbiddenException.class);assertThat(db.queryForObject("SELECT count(*) FROM data_rights_requests WHERE user_id=?",Integer.class,user)).isZero();}
 @Test @Tag("malicious") void defaultPublicationRemainsDisabledEvenForAdmin(){UUID user=owner();db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='ADMIN'",user);assertThatThrownBy(()->publication.publish(UUID.randomUUID(),user)).isInstanceOf(com.greenocean.backend.common.exception.ForbiddenException.class);}
 @Test @Tag("confused") void intakeCannotStartWithoutAccountableOwnerAndOperationalTarget(){assertThatThrownBy(()->new PrivacyGovernanceService(null,null,false,false,true,"",0)).isInstanceOf(IllegalArgumentException.class);}
}
