package com.greenocean.backend;

import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import com.greenocean.backend.trust.*;
import com.greenocean.backend.common.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties="greenocean.trust.enabled=true")
@ActiveProfiles("test") @Transactional
class PhaseQ4RecoveryTest {
    @Autowired JdbcTemplate db;
    @Autowired UserRepository users;
    @Autowired TokenService tokens;
    @Autowired TrustAuthority authority;
    @Autowired com.greenocean.backend.account.service.AccountService accounts;
    PhaseIFixtures.Actor first, second, subject;
    RecoveryReplay replay;
    List<RecoveryReplay.Subject> restrictions;
    final String source="035879e196e99ed1ffc1097c3363acf7287040b0";
    String digest;

    PhaseIFixtures.Actor operator() {
        var actor=PhaseIFixtures.member(db,users,tokens);
        db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='TRUST_ACCOUNT'",actor.id());
        db.update("UPDATE user_sessions SET mfa_authenticated_at=CURRENT_TIMESTAMP WHERE id=?",actor.session());
        return actor;
    }
    @BeforeEach void setup() {
        PhaseIFixtures.region(db);
        first=operator(); second=operator(); subject=PhaseIFixtures.member(db,users,tokens);
        restrictions=List.of(new RecoveryReplay.Subject(subject.id(),"ALL"));
        digest=RecoveryReplay.manifestDigest(source,restrictions,List.of("CLINIC"));
        replay=new RecoveryReplay(db,authority,UUID.randomUUID().toString(),digest,source,first.id()+","+second.id());
    }
    UUID prepare(){return replay.prepare(first.id(),first.session(),source,digest,restrictions,List.of("CLINIC"));}
    void approve(UUID id){replay.review(second.id(),second.session(),id);}

    @Test void normalModeDoesNotActivateRestoreRestrictions(){
        var normal=new RecoveryReplay(db,authority,"","","","");
        assertThat(normal.ready()).isTrue(); assertThat(normal.configured()).isFalse();
        assertThat(normal.restricted(subject.id(),"SOCIAL")).isFalse();
        assertThat(normal.stopped("CLINIC")).isFalse();
    }
    @Test void configuredRecoveryIsClosedUntilIndependentReview(){
        assertThat(replay.ready()).isFalse();UUID id=prepare();
        assertThat(replay.ready()).isFalse();
        assertThatThrownBy(()->replay.review(first.id(),first.session(),id)).isInstanceOf(ForbiddenException.class);
        approve(id);assertThat(replay.ready()).isTrue();
        assertThat(replay.restricted(subject.id(),"WELLBEING")).isTrue();
        assertThat(replay.stopped("CLINIC")).isTrue();
        assertThat(db.queryForObject("SELECT revoked_at IS NOT NULL FROM user_sessions WHERE id=?",Boolean.class,subject.session())).isTrue();
    }
    @Test void matchingHashCannotAuthorizeSubstitutedPayload(){
        assertThatThrownBy(()->replay.prepare(first.id(),first.session(),source,digest,List.of(),List.of("CLINIC"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->replay.prepare(first.id(),first.session(),source,digest,restrictions,List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThat(replay.ready()).isFalse();
    }
    @Test void canonicalDigestIgnoresOrderingButRejectsDuplicateSubjects(){
        var a=new RecoveryReplay.Subject(first.id(),"WELLBEING");var b=new RecoveryReplay.Subject(subject.id(),"CLINIC");
        assertThat(RecoveryReplay.manifestDigest(source,List.of(a,b),List.of("SOCIAL","CLINIC")))
            .isEqualTo(RecoveryReplay.manifestDigest(source,List.of(b,a),List.of("CLINIC","SOCIAL")));
        assertThatThrownBy(()->RecoveryReplay.manifestDigest(source,List.of(a,a),List.of())).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void malformedNestedManifestEntriesFailAsValidationErrors(){
        assertThatThrownBy(()->RecoveryReplay.manifestDigest(source,Collections.singletonList(null),List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->RecoveryReplay.manifestDigest(source,List.of(new RecoveryReplay.Subject(subject.id(),null)),List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->RecoveryReplay.manifestDigest(source,restrictions,Collections.singletonList(null))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->RecoveryReplay.manifestDigest(source,restrictions,List.of("CLINIC","CLINIC"))).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void retryIsIdempotentAndEpochCannotBeTakenOver(){
        UUID id=prepare();assertThat(prepare()).isEqualTo(id);
        assertThatThrownBy(()->replay.prepare(second.id(),second.session(),source,digest,restrictions,List.of("CLINIC"))).isInstanceOf(ConflictException.class);
        assertThat(replay.detail(second.id(),second.session(),id).toString()).contains(subject.id().toString(),digest);
    }
    @Test void stalePreparerCannotObtainReview(){
        UUID id=prepare();db.update("UPDATE user_sessions SET revoked_at=CURRENT_TIMESTAMP WHERE id=?",first.session());
        assertThatThrownBy(()->approve(id)).isInstanceOf(UnauthorizedException.class);
        assertThat(replay.ready()).isFalse();
    }
    @Test void removingAndRegrantingAuthorityCannotReviveApproval(){
        UUID id=prepare();approve(id);
        db.update("DELETE FROM user_roles WHERE user_id=? AND role_id IN(SELECT id FROM roles WHERE name='TRUST_ACCOUNT')",second.id());
        db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='TRUST_ACCOUNT'",second.id());
        assertThat(replay.ready()).isFalse();
    }
    @Test void accountRestrictionPermanentlyInvalidatesEpoch(){
        UUID id=prepare();approve(id);
        db.update("UPDATE users SET email_verified=FALSE WHERE id=?",first.id());
        db.update("UPDATE users SET email_verified=TRUE WHERE id=?",first.id());
        assertThat(replay.ready()).isFalse();
    }
    @Test void explicitStopAndImmutableHistoryCannotBeBypassed(){
        UUID id=prepare();approve(id);replay.invalidate(second.id(),second.session(),id);
        assertThat(replay.ready()).isFalse();
        assertThatThrownBy(()->db.update("DELETE FROM recovery_replay_revocations WHERE run_id=?",id)).isInstanceOf(org.springframework.dao.DataAccessException.class);
    }
    @Test void unprivilegedUserCannotPrepareOrInspect(){
        assertThatThrownBy(()->replay.status(subject.id(),subject.session())).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(()->replay.prepare(subject.id(),subject.session(),source,digest,restrictions,List.of("CLINIC"))).isInstanceOf(ForbiddenException.class);
    }
    @Test void malformedExternalConfigurationFailsStartup(){
        assertThatThrownBy(()->new RecoveryReplay(db,authority,UUID.randomUUID().toString(),"",source,first.id()+","+second.id())).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void accountArchiveContainsOnlyOwnReplayMetadata(){
        UUID id=prepare();approve(id);
        String own=accounts.archive(first.id()).trustAndSecurity().toString();
        assertThat(own).contains("ownRecoveryPreparations",id.toString()).doesNotContain(subject.id().toString(),second.id().toString(),first.session().toString());
        String affected=accounts.archive(subject.id()).trustAndSecurity().toString();
        assertThat(affected).contains("ownRestoredRestrictions","ALL").doesNotContain(id.toString(),first.id().toString(),second.id().toString());
    }
    @Test void operatorRevokedOutsideTheBackupCannotPrepareTheirOwnReplay(){
        var manifest=List.of(new RecoveryReplay.Subject(first.id(),"ALL"));
        String hash=RecoveryReplay.manifestDigest(source,manifest,List.of());
        var restored=new RecoveryReplay(db,authority,UUID.randomUUID().toString(),hash,source,first.id()+","+second.id());
        assertThatThrownBy(()->restored.prepare(first.id(),first.session(),source,hash,manifest,List.of())).isInstanceOf(ForbiddenException.class);
    }
    @Test void revokedReviewerCannotUseOldBackupAuthorityToApproveReplay(){
        var manifest=List.of(new RecoveryReplay.Subject(second.id(),"ALL"));
        String hash=RecoveryReplay.manifestDigest(source,manifest,List.of());
        var restored=new RecoveryReplay(db,authority,UUID.randomUUID().toString(),hash,source,first.id()+","+second.id());
        UUID id=restored.prepare(first.id(),first.session(),source,hash,manifest,List.of());
        assertThatThrownBy(()->restored.review(second.id(),second.session(),id)).isInstanceOf(ForbiddenException.class);
    }
    @Test void historicalUnsafeApprovalCannotReactivateWithRevokedReviewer(){
        var manifest=List.of(new RecoveryReplay.Subject(second.id(),"ALL"));
        String hash=RecoveryReplay.manifestDigest(source,manifest,List.of());
        var restored=new RecoveryReplay(db,authority,UUID.randomUUID().toString(),hash,source,first.id()+","+second.id());
        UUID id=restored.prepare(first.id(),first.session(),source,hash,manifest,List.of());
        // Explicit old-version synthetic state, not a production approval/import endpoint.
        db.update("INSERT INTO recovery_replay_reviews(run_id,reviewer_id,review_session) VALUES(?,?,?)",id,second.id(),second.session());
        assertThat(restored.ready()).isFalse();
    }
    @Test void oldBackupRoleDoesNotSubstituteForCurrentExternalOperatorAuthorization(){
        var outside=operator();
        assertThatThrownBy(()->replay.prepare(outside.id(),outside.session(),source,digest,restrictions,List.of("CLINIC"))).isInstanceOf(ForbiddenException.class);
    }
    @Test void configuredRestoreWithoutTwoDistinctExternalOperatorsFailsClosed(){
        assertThatThrownBy(()->new RecoveryReplay(db,authority,UUID.randomUUID().toString(),digest,source,first.id().toString())).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void currentOperatorsCannotReadOrInvalidateAHistoricalEpoch(){
        UUID old=prepare();var next=new RecoveryReplay(db,authority,UUID.randomUUID().toString(),digest,source,first.id()+","+second.id());
        assertThatThrownBy(()->next.detail(first.id(),first.session(),old)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(()->next.invalidate(first.id(),first.session(),old)).isInstanceOf(NotFoundException.class);
    }
}
