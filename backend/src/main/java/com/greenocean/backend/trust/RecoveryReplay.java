package com.greenocean.backend.trust;

import com.greenocean.backend.common.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;

@Service @Transactional
public class RecoveryReplay {
    private final JdbcTemplate db;
    private final TrustAuthority authority;
    private final String epoch,digest,source;
    private final Set<UUID> operators;

    public RecoveryReplay(JdbcTemplate db,TrustAuthority authority,
            @Value("${greenocean.recovery.restore-epoch:}")String epoch,
            @Value("${greenocean.recovery.manifest-sha256:}")String digest,
            @Value("${greenocean.recovery.source-commit:}")String source,
            @Value("${greenocean.recovery.operator-ids:}")String operatorIds){
        this.db=db;this.authority=authority;this.epoch=epoch;this.digest=digest;this.source=source;
        var declared=Arrays.stream(operatorIds.split(",")).map(String::trim).filter(value->!value.isEmpty()).map(UUID::fromString).toList();
        operators=Set.copyOf(declared);
        if(declared.size()!=operators.size()||operators.size()>10)throw new IllegalArgumentException("Declare distinct externally verified recovery operators");
        if(!epoch.isBlank()){
            UUID.fromString(epoch);
            if(!digest.matches("[a-f0-9]{64}")||!source.matches("[a-f0-9]{40}")||operators.size()<2)
                throw new IllegalArgumentException("Restore requires a fresh external epoch,exact manifest/source and at least two externally authorized operators");
        }
    }
    public record Subject(UUID userId,String domain){}
    public static String manifestDigest(String commit,List<Subject> subjects,List<String> stops){
        Set<String> services=Set.of("SOCIAL","WELLBEING","LISTENER","CLINIC","FINANCE");
        Set<String> purposes=Set.of("ALL","SOCIAL","WELLBEING","LISTENER","CLINIC","FINANCE");
        if(commit==null||!commit.matches("[a-f0-9]{40}")||subjects==null||subjects.size()>5000||
            stops==null||stops.size()>5||stops.stream().anyMatch(Objects::isNull)||
            new HashSet<>(stops).size()!=stops.size()||!services.containsAll(stops))
            throw new IllegalArgumentException("Bounded exact restore manifest required");
        var lines=new TreeSet<String>();
        for(var subject:subjects){
            if(subject==null||subject.userId()==null||subject.domain()==null||!purposes.contains(subject.domain())||!lines.add(subject.userId()+":"+subject.domain()))
                throw new IllegalArgumentException("Distinct valid subject/domain entries required");
        }
        String canonical="GOREPLAY1\n"+commit+"\n"+String.join("\n",lines)+"\n"+String.join(",",new TreeSet<>(stops));
        try{return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8)));}
        catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
    }
    public boolean configured(){return !epoch.isBlank();}
    private void operator(UUID who,UUID sid){
        authority.require(who,sid,"ACCOUNT");
        if(configured()&&(!operators.contains(who)||restricted(who,"ALL")))
            throw new ForbiddenException("Current external recovery authority is required; revoked subjects cannot authorize replay");
    }
    public UUID prepare(UUID who,UUID sid,String commit,String hash,List<Subject> subjects,List<String> stops){
        operator(who,sid);
        if(!configured()||!source.equals(commit)||!digest.equals(hash)||!digest.equals(manifestDigest(commit,subjects,stops)))
            throw new IllegalArgumentException("Review the exact externally configured restore manifest and bounded restrictions");
        if(subjects.stream().anyMatch(subject->who.equals(subject.userId())&&"ALL".equals(subject.domain())))
            throw new ForbiddenException("An operator revoked by the current external manifest cannot prepare replay");
        authority.budget(who,"RECOVERY_REPLAY_PREPARE",5);
        db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))","restore-epoch:"+epoch);
        var existing=db.queryForList("SELECT * FROM recovery_replay_runs WHERE epoch=?",UUID.fromString(epoch));
        if(!existing.isEmpty()){
            var old=existing.getFirst();
            if(!who.equals(old.get("prepared_by"))||!sid.equals(old.get("prepared_session")))throw new ConflictException("This external restore epoch already belongs to another preparation");
            return(UUID)old.get("id");
        }
        UUID id=db.queryForObject("INSERT INTO recovery_replay_runs(epoch,source_commit,manifest_sha256,prepared_by,prepared_session,expires_at) VALUES(?,?,?,?,?,CURRENT_TIMESTAMP+INTERVAL '1 hour') RETURNING id",UUID.class,UUID.fromString(epoch),commit,hash,who,sid);
        for(var restriction:subjects)db.update("INSERT INTO recovery_replay_subjects(run_id,subject_id,domain) VALUES(?,?,?)",id,restriction.userId(),restriction.domain());
        for(String stop:stops)db.update("INSERT INTO recovery_replay_stops(run_id,service) VALUES(?,?)",id,stop);
        authority.audit(who,id,"RECOVERY_REPLAY_PREPARED");return id;
    }
    public void review(UUID who,UUID sid,UUID id){
        operator(who,sid);
        if(!configured())throw new ConflictException("Restore mode is not configured");
        var rows=db.queryForList("SELECT * FROM recovery_replay_runs WHERE id=? AND epoch=? AND source_commit=? AND manifest_sha256=? AND expires_at>CURRENT_TIMESTAMP FOR UPDATE",id,UUID.fromString(epoch),source,digest);
        if(rows.isEmpty())throw new NotFoundException("Current exact restore review required");
        var run=rows.getFirst();operator((UUID)run.get("prepared_by"),(UUID)run.get("prepared_session"));
        if(who.equals(run.get("prepared_by")))throw new ForbiddenException("A second independent privacy operator must verify replay");
        if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM recovery_replay_revocations WHERE run_id=?)",Boolean.class,id)))
            throw new ConflictException("Recovery approval was revoked; use a fresh external epoch");
        if(db.update("INSERT INTO recovery_replay_reviews(run_id,reviewer_id,review_session) VALUES(?,?,?) ON CONFLICT DO NOTHING",id,who,sid)==1){
            db.update("UPDATE user_sessions SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE user_id IN(SELECT subject_id FROM recovery_replay_subjects WHERE run_id=? AND domain='ALL')",id);
            authority.audit(who,id,"RECOVERY_REPLAY_INDEPENDENTLY_VERIFIED");
        }
    }
    public void invalidate(UUID who,UUID sid,UUID id){
        operator(who,sid);currentRun(id);
        db.update("INSERT INTO recovery_replay_revocations(run_id,subject_id,reason) VALUES(?,?,'EXPLICIT_STOP') ON CONFLICT DO NOTHING",id,who);
        authority.audit(who,id,"RECOVERY_REPLAY_INVALIDATED");
    }
    public boolean ready(){
        if(!configured())return true;
        return Boolean.TRUE.equals(db.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM recovery_replay_runs r
            JOIN recovery_replay_reviews v ON v.run_id=r.id JOIN users a ON a.id=r.prepared_by JOIN users b ON b.id=v.reviewer_id
            WHERE r.epoch=? AND r.source_commit=? AND r.manifest_sha256=?
            AND r.prepared_by=ANY(?::uuid[]) AND v.reviewer_id=ANY(?::uuid[])
            AND a.status='ACTIVE' AND a.email_verified AND a.deleted_at IS NULL
            AND b.status='ACTIVE' AND b.email_verified AND b.deleted_at IS NULL
            AND NOT EXISTS(SELECT 1 FROM recovery_replay_revocations x WHERE x.run_id=r.id)
            AND NOT EXISTS(SELECT 1 FROM recovery_replay_subjects s WHERE s.run_id=r.id AND s.domain='ALL' AND s.subject_id IN(r.prepared_by,v.reviewer_id))
            AND EXISTS(SELECT 1 FROM user_roles u JOIN roles role ON role.id=u.role_id WHERE u.user_id=a.id AND role.name='TRUST_ACCOUNT')
            AND EXISTS(SELECT 1 FROM user_roles u JOIN roles role ON role.id=u.role_id WHERE u.user_id=b.id AND role.name='TRUST_ACCOUNT'))
            """,Boolean.class,UUID.fromString(epoch),source,digest,(Object)operators.toArray(UUID[]::new),(Object)operators.toArray(UUID[]::new)));
    }
    public boolean restricted(UUID subject,String domain){
        return configured()&&Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM recovery_replay_subjects s JOIN recovery_replay_runs r ON r.id=s.run_id WHERE r.epoch=? AND s.subject_id=? AND s.domain IN('ALL',?))",Boolean.class,UUID.fromString(epoch),subject,domain));
    }
    public boolean stopped(String domain){
        return configured()&&Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM recovery_replay_stops s JOIN recovery_replay_runs r ON r.id=s.run_id WHERE r.epoch=? AND s.service=?)",Boolean.class,UUID.fromString(epoch),domain));
    }
    public Object status(UUID who,UUID sid){
        operator(who,sid);
        return Map.of("restoreMode",configured(),"replayReady",ready(),"runtimeServiceAuthorizationUnchanged",true,"runs",configured()?
            db.queryForList("SELECT r.id,r.source_commit,r.manifest_sha256,r.prepared_by,r.expires_at,v.reviewer_id FROM recovery_replay_runs r LEFT JOIN recovery_replay_reviews v ON v.run_id=r.id WHERE r.epoch=?",UUID.fromString(epoch)):List.of());
    }
    private Map<String,Object> currentRun(UUID id){
        if(!configured())throw new NotFoundException("Current restore run unavailable");
        var rows=db.queryForList("SELECT id,source_commit,manifest_sha256,prepared_by,expires_at FROM recovery_replay_runs WHERE id=? AND epoch=? AND source_commit=? AND manifest_sha256=?",id,UUID.fromString(epoch),source,digest);
        if(rows.isEmpty())throw new NotFoundException("Current restore run unavailable");return rows.getFirst();
    }
    public Object detail(UUID who,UUID sid,UUID id){
        operator(who,sid);var run=currentRun(id);
        return Map.of("run",run,"subjects",db.queryForList("SELECT subject_id,domain FROM recovery_replay_subjects WHERE run_id=? ORDER BY subject_id,domain",id),"stoppedServices",db.queryForList("SELECT service FROM recovery_replay_stops WHERE run_id=? ORDER BY service",id));
    }
}
