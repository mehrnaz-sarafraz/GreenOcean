package com.greenocean.backend;

import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import com.greenocean.backend.trust.RecoveryReplay;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

/** Runs actual HTTP replay checks only against a fresh synthetic restored database. */
public final class PhaseQ4RestoredApplicationRunner {
    static int checks;
    static final String SOURCE="035879e196e99ed1ffc1097c3363acf7287040b0";
    public static void main(String[] args)throws Exception {
        String url=System.getenv("GREENOCEAN_TEST_DB_URL");
        if(!"isolated-only".equals(System.getenv("GREENOCEAN_RESTORE_QA"))||url==null||
            !url.matches("jdbc:postgresql://127\\.0\\.0\\.1:55432/greenocean_q4_restore_[a-f0-9]{32}_qa"))
            throw new IllegalStateException("Fresh uniquely named disposable restored database required");
        var initial=new JdbcTemplate(new DriverManagerDataSource(url,"postgres",""));
        var clinical=initial.queryForList("SELECT e.id,r.patient_id,r.professional_id FROM clinic_encounters e JOIN clinic_care_relationships r ON r.id=e.relationship_id ORDER BY e.id DESC LIMIT 1");
        UUID clinicalPatient=clinical.isEmpty()?new UUID(0,0):(UUID)clinical.getFirst().get("patient_id");
        var subjects=initial.queryForList("SELECT u.id FROM users u WHERE u.email LIKE 'i-%@example.test' AND u.id<>? AND EXISTS(SELECT 1 FROM wellbeing_journal_entries j WHERE j.user_id=u.id) AND EXISTS(SELECT 1 FROM user_sessions s WHERE s.user_id=u.id AND s.revoked_at IS NULL AND s.expires_at>CURRENT_TIMESTAMP AND s.overall_expires_at>CURRENT_TIMESTAMP AND s.idle_expires_at>CURRENT_TIMESTAMP) ORDER BY u.created_at DESC,u.id LIMIT 3",UUID.class,clinicalPatient);
        if(subjects.size()!=3)throw new IllegalStateException("Representative synthetic restored subjects required");
        var restrictions=new ArrayList<>(List.of(new RecoveryReplay.Subject(subjects.get(0),"ALL"),new RecoveryReplay.Subject(subjects.get(1),"WELLBEING")));
        if(!clinical.isEmpty())restrictions.add(new RecoveryReplay.Subject(clinicalPatient,"CLINIC"));
        String digest=RecoveryReplay.manifestDigest(SOURCE,restrictions,List.of("CLINIC"));
        long start=System.nanoTime();
        try(var ctx=new SpringApplicationBuilder(BackendApplication.class).profiles("test").run(
            "--server.address=127.0.0.1","--server.port=0","--spring.datasource.url="+url,
            "--spring.datasource.username=postgres","--spring.datasource.password=","--greenocean.trust.enabled=true",
            "--greenocean.clinic.commerce-enabled=true","--greenocean.clinic.records-enabled=true",
            "--greenocean.clinic.record-keys=clinical-qa:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","--greenocean.clinic.record-current-key=clinical-qa",
            "--greenocean.recovery.restore-epoch="+UUID.randomUUID(),"--greenocean.recovery.source-commit="+SOURCE,
            "--greenocean.recovery.manifest-sha256="+digest)){
            var db=ctx.getBean(JdbcTemplate.class);var users=ctx.getBean(UserRepository.class);var tokens=ctx.getBean(TokenService.class);
            var json=ctx.getBean(ObjectMapper.class);PhaseIFixtures.region(db);
            var first=operator(db,users,tokens);var second=operator(db,users,tokens);
            var restoredTokens=new ArrayList<String>();
            for(UUID subject:subjects)restoredTokens.add(restoredToken(db,users,tokens,subject));
            String base="http://127.0.0.1:"+ctx.getEnvironment().getRequiredProperty("local.server.port");
            var client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
            check(send(client,base,"GET","/api/v1/wellbeing/journal",restoredTokens.get(2),null).statusCode(),403);
            var prepared=send(client,base,"POST","/api/v1/recovery/prepare",first.token(),json.writeValueAsString(Map.of(
                "sourceCommit",SOURCE,"manifestSha256",digest,"subjects",restrictions,"stoppedServices",List.of("CLINIC"))));
            check(prepared.statusCode(),201);String run=json.readTree(prepared.body()).path("id").asText();
            check(send(client,base,"POST","/api/v1/recovery/runs/"+run+"/review",first.token(),null).statusCode(),403);
            check(send(client,base,"GET","/api/v1/recovery/runs/"+run,second.token(),null).statusCode(),200);
            check(send(client,base,"POST","/api/v1/recovery/runs/"+run+"/review",second.token(),null).statusCode(),204);
            check(send(client,base,"GET","/api/v1/wellbeing/journal",restoredTokens.get(0),null).statusCode(),401);
            check(send(client,base,"GET","/api/v1/wellbeing/journal",restoredTokens.get(1),null).statusCode(),403);
            check(send(client,base,"GET","/api/v1/wellbeing/journal",restoredTokens.get(2),null).statusCode(),200);
            if(!clinical.isEmpty()){
                String clinicalPath="/api/v1/clinical/encounters/"+clinical.getFirst().get("id");
                check(send(client,base,"GET",clinicalPath,restoredToken(db,users,tokens,clinicalPatient),null).statusCode(),403);
                var currentProfessional=PhaseIFixtures.session(db,users,tokens,(UUID)clinical.getFirst().get("professional_id"));
                db.update("UPDATE user_sessions SET mfa_authenticated_at=CURRENT_TIMESTAMP,reauthenticated_at=CURRENT_TIMESTAMP WHERE id=?",currentProfessional.session());
                check(send(client,base,"GET",clinicalPath,currentProfessional.token(),null).statusCode(),200);
                check(send(client,base,"GET",clinicalPath+"/messages",restoredToken(db,users,tokens,clinicalPatient),null).statusCode(),403);
                var messages=send(client,base,"GET",clinicalPath+"/messages",currentProfessional.token(),null);
                check(messages.statusCode(),200);
                if(!messages.body().contains("SYNTHETIC-Q4-RESTORED-PRIVATE-CLINICAL-MESSAGE"))throw new AssertionError("Expected authenticated decrypted synthetic message was not restored");
            }
            check(send(client,base,"POST","/api/v1/clinic/appointments",restoredTokens.get(2),"{}").statusCode(),403);
            db.update("DELETE FROM user_roles WHERE user_id=? AND role_id=(SELECT id FROM roles WHERE name='TRUST_ACCOUNT')",second.id());
            check(send(client,base,"GET","/api/v1/wellbeing/journal",restoredTokens.get(2),null).statusCode(),403);
            db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='TRUST_ACCOUNT'",second.id());
            check(send(client,base,"GET","/api/v1/wellbeing/journal",restoredTokens.get(2),null).statusCode(),403);
            Files.writeString(Path.of("target","phase-q4-restored-application.json"),json.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of(
                "evidenceKind","SYNTHETIC_LOCAL_RESTORED_APPLICATION_HTTP","sourceBaseCommit",SOURCE,"modifiedQ4Worktree",true,
                "bootAndReplayExerciseSeconds",(System.nanoTime()-start)/1e9,"checksPassed",checks,
                "privacyReplayVerified",true,"independentOperatorCheck",true,"oldSessionRevoked",true,"privateClinicalMessageVerified",!clinical.isEmpty(),
                "limitations",List.of("Synthetic MFA stamps, not a real operator/key custody drill","Base commit plus uncommitted Q4 sources; repeat on final immutable head","Current external rights manifest was synthetic","Private-journal reads, available synthetic clinical episode, appointment denial and replay APIs probed; no comprehensive service RTO proven"))));
            System.out.println("Q4 restored application HTTP replay verified with independent purpose operators");
        }
    }
    static PhaseIFixtures.Actor operator(JdbcTemplate db,UserRepository users,TokenService tokens){
        var actor=PhaseIFixtures.member(db,users,tokens);
        db.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name='TRUST_ACCOUNT'",actor.id());
        var fresh=PhaseIFixtures.session(db,users,tokens,actor.id());
        db.update("UPDATE user_sessions SET mfa_authenticated_at=CURRENT_TIMESTAMP WHERE id=?",fresh.session());
        return fresh;
    }
    static String restoredToken(JdbcTemplate db,UserRepository users,TokenService tokens,UUID subject){
        String email=db.queryForObject("SELECT email FROM users WHERE id=?",String.class,subject);
        UUID session=db.queryForObject("SELECT id FROM user_sessions WHERE user_id=? AND revoked_at IS NULL AND expires_at>CURRENT_TIMESTAMP AND overall_expires_at>CURRENT_TIMESTAMP AND idle_expires_at>CURRENT_TIMESTAMP ORDER BY created_at DESC LIMIT 1",UUID.class,subject);
        // Fresh signature, actual restored session: the original session is neither renewed nor replaced.
        return tokens.createAccessToken(users.findByEmailIgnoreCase(email).orElseThrow(),session);
    }
    static HttpResponse<String> send(HttpClient client,String base,String method,String path,String token,String body)throws Exception{
        return client.send(HttpRequest.newBuilder(URI.create(base+path)).timeout(Duration.ofSeconds(10))
            .header("Authorization","Bearer "+token).header("Content-Type","application/json")
            .method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    static void check(int actual,int expected){if(actual!=expected)throw new AssertionError("Restored application expected HTTP "+expected+" but received "+actual);checks++;}
}
