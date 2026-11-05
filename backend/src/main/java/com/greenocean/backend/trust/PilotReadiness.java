package com.greenocean.backend.trust;

import com.greenocean.backend.common.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.time.*;
import java.util.*;

@Service @Transactional
public class PilotReadiness {
    public static final Set<String> SERVICES=Set.of("COMMUNITY","WELLBEING_EDUCATION","LISTENERS","PROFESSIONAL_BOOKING","CLINICAL_TEXT","CLINICAL_MEDIA","REAL_PAYMENTS","CLINICAL_RECORDS");
    public static final Set<String> CRITERIA=Set.of("SOURCE","CI","SECURITY","PRIVACY","LEGAL","STAFFING","PROVIDERS","MONITORING","BACKUP_RESTORE","INCIDENT_RESPONSE","DEVICES","CAPACITY","ROLLBACK");
    static final Set<String> TECHNICAL=Set.of("SOURCE","CI","SECURITY","BACKUP_RESTORE","INCIDENT_RESPONSE","DEVICES","CAPACITY","ROLLBACK");
    public static final Set<String> KINDS=Set.of("SYNTHETIC","CI","INDEPENDENT_REVIEW","LEGAL_APPROVAL","OPERATIONS","PROVIDER","PHYSICAL_DEVICE","STAGING_RESTORE","PRODUCTION_RESTORE","CAPACITY_TEST");
    private final JdbcTemplate db;private final TrustAuthority authority;private final PilotDependencyChecks dependencies;private final String source;private final Set<UUID> owners;
    public PilotReadiness(JdbcTemplate db,TrustAuthority authority,PilotDependencyChecks dependencies,@Value("${greenocean.pilot.source-commit:}")String source,@Value("${greenocean.pilot.owner-ids:}")String owners){
        this.db=db;this.authority=authority;this.dependencies=dependencies;this.source=source;this.owners=new HashSet<>();for(String id:owners.split(","))if(!id.isBlank())this.owners.add(UUID.fromString(id.trim()));
        if(!source.isBlank()&&!source.matches("[a-f0-9]{40}"))throw new IllegalArgumentException("Pilot source must be an exact deployed commit");
    }
    public record Evidence(long revision,String criterion,String state,String kind,String reference,String artifactSha256,List<String> exactTests,String responsibleRole,List<String> risks,Instant expiresAt){}
    public static String domain(String service){if(service==null||!SERVICES.contains(service))throw new IllegalArgumentException("Choose a pilot service");return switch(service){case "COMMUNITY"->"SOCIAL";case "WELLBEING_EDUCATION"->"EDUCATION";case "LISTENERS"->"LISTENER";case "REAL_PAYMENTS"->"FINANCE";default->"CLINIC";};}
    public String source(){return source;}
    void staff(UUID who,UUID sid){authority.require(who,sid,"PLATFORM");}
    void owner(UUID who,UUID sid){staff(who,sid);if(!owners.contains(who)||!authority.has(who,"PILOT_OWNER"))throw new ForbiddenException("A currently designated independent owner must act");}
    static String hash(String value){try{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));}catch(Exception impossible){throw new IllegalStateException(impossible);}}
    Map<String,Object> notice(){var rows=db.queryForList("SELECT version,title,summary,content FROM policy_documents WHERE policy_key='pilot-participation' AND status='PUBLISHED' AND published_at<=CURRENT_TIMESTAMP ORDER BY version DESC LIMIT 1");return rows.isEmpty()?Map.of():rows.getFirst();}
    Map<String,Object> row(UUID id,boolean lock){var rows=db.queryForList("SELECT * FROM pilot_assessments WHERE id=?"+(lock?" FOR UPDATE":""),id);if(rows.isEmpty())throw new NotFoundException("Pilot assessment unavailable");return rows.getFirst();}
    void revision(Map<String,Object> row,long revision){if(((Number)row.get("revision")).longValue()!=revision)throw new ConflictException("Refresh the exact assessment revision");}
    public UUID create(UUID who,UUID sid,String service,String country,String commit){
        staff(who,sid);domain(service);if(country==null||!country.matches("[A-Z]{2}")||commit==null||!commit.matches("[a-f0-9]{40}"))throw new IllegalArgumentException("Exact service,country and commit required");
        authority.budget(who,"PILOT_ASSESSMENT_CREATE",30);var notice=notice();
        UUID id=db.queryForObject("INSERT INTO pilot_assessments(service,country_code,source_commit,notice_version,notice_sha256,created_by,created_session) VALUES(?,?,?,?,?,?,?) RETURNING id",UUID.class,service,country,commit,notice.get("version"),notice.isEmpty()?null:hash((String)notice.get("content")),who,sid);
        authority.audit(who,id,"PILOT_ASSESSMENT_CREATED");return id;
    }
    public UUID evidence(UUID who,UUID sid,UUID id,Evidence input){
        staff(who,sid);if(input==null)throw new IllegalArgumentException("Evidence required");var assessment=row(id,true);revision(assessment,input.revision());
        if(input.criterion()==null||input.state()==null||input.kind()==null||!CRITERIA.contains(input.criterion())||!Set.of("PASS","FAIL","BLOCKED","APPROVAL_REQUIRED","DEVICE_TEST_REQUIRED").contains(input.state())||!KINDS.contains(input.kind())||input.reference()==null||!input.reference().matches("[A-Z][A-Z0-9_-]{2,40}:[A-Za-z0-9._-]{1,120}")||input.artifactSha256()==null||!input.artifactSha256().matches("[a-f0-9]{64}")||input.responsibleRole()==null||!input.responsibleRole().matches("[A-Z][A-Z0-9_]{2,60}")||input.exactTests()==null||input.exactTests().isEmpty()||input.exactTests().size()>30||input.exactTests().stream().anyMatch(s->s==null||!s.matches("[A-Za-z0-9._:#/-]{1,150}"))||input.risks()==null||input.risks().size()>20||input.risks().stream().anyMatch(s->s==null||!s.matches("[A-Z0-9_]{2,80}"))||input.expiresAt()==null||!input.expiresAt().isAfter(Instant.now())||input.expiresAt().isAfter(Instant.now().plusSeconds(30*86400L)))throw new IllegalArgumentException("Bounded actual evidence,exact tests,responsible role,risks and future expiry required");
        authority.budget(who,"PILOT_EVIDENCE_CREATE",200);
        try{
            var json=new tools.jackson.databind.ObjectMapper();
            UUID evidence=db.queryForObject("INSERT INTO pilot_evidence(assessment_id,criterion,state,kind,reference,artifact_sha256,exact_tests,responsible_role,risks,author_id,author_session,expires_at) VALUES(?,?,?,?,?,?,?::jsonb,?,?::jsonb,?,?,?) RETURNING id",UUID.class,id,input.criterion(),input.state(),input.kind(),input.reference(),input.artifactSha256(),json.writeValueAsString(input.exactTests()),input.responsibleRole(),json.writeValueAsString(input.risks()),who,sid,java.sql.Timestamp.from(input.expiresAt()));
            db.update("UPDATE pilot_assessments SET revision=revision+1 WHERE id=?",id);authority.audit(who,id,"PILOT_EVIDENCE_RECORDED");return evidence;
        }catch(tools.jackson.core.JacksonException invalid){throw new IllegalArgumentException("Evidence format rejected",invalid);}
    }
    public void review(UUID who,UUID sid,UUID id,UUID evidence,long expected){
        staff(who,sid);var assessment=row(id,true);revision(assessment,expected);
        var rows=db.queryForList("SELECT e.* FROM pilot_evidence e WHERE e.id=? AND e.assessment_id=? AND e.expires_at>CURRENT_TIMESTAMP AND e.id=(SELECT n.id FROM pilot_evidence n WHERE n.assessment_id=e.assessment_id AND n.criterion=e.criterion ORDER BY n.created_at DESC,n.id DESC LIMIT 1)",evidence,id);
        if(rows.isEmpty())throw new NotFoundException("Current exact evidence required");var e=rows.getFirst();
        if(who.equals(e.get("author_id")))throw new ForbiddenException("Independent evidence review required");
        if(!authority.has((UUID)e.get("author_id"),"TRUST_PLATFORM"))throw new ForbiddenException("Evidence author authority is no longer current");
        if(db.update("INSERT INTO pilot_evidence_reviews(evidence_id,reviewer_id,review_session) VALUES(?,?,?) ON CONFLICT DO NOTHING",evidence,who,sid)==1){db.update("UPDATE pilot_assessments SET revision=revision+1 WHERE id=?",id);authority.audit(who,id,"PILOT_EVIDENCE_INDEPENDENTLY_REVIEWED");}
    }
    List<Map<String,Object>> evidenceRows(UUID id){var rows=db.queryForList("SELECT DISTINCT ON(e.criterion) e.*,v.reviewer_id,v.created_at AS reviewed_at FROM pilot_evidence e LEFT JOIN pilot_evidence_reviews v ON v.evidence_id=e.id WHERE e.assessment_id=? ORDER BY e.criterion,e.created_at DESC,e.id DESC",id);for(var row:rows){for(String field:List.of("exact_tests","risks"))row.put(field,new tools.jackson.databind.ObjectMapper().readValue(row.get(field).toString(),List.class));row.remove("author_session");}return rows;}
    boolean acceptable(Map<String,Object> e){
        if(!"PASS".equals(e.get("state"))||"SYNTHETIC".equals(e.get("kind"))||e.get("reviewer_id")==null||!time(e.get("expires_at")).isAfter(Instant.now())||!authority.has((UUID)e.get("author_id"),"TRUST_PLATFORM")||!authority.has((UUID)e.get("reviewer_id"),"TRUST_PLATFORM"))return false;
        return switch((String)e.get("criterion")){case "DEVICES"->"PHYSICAL_DEVICE".equals(e.get("kind"));case "LEGAL"->"LEGAL_APPROVAL".equals(e.get("kind"));case "SECURITY"->"INDEPENDENT_REVIEW".equals(e.get("kind"));case "CI"->"CI".equals(e.get("kind"));case "BACKUP_RESTORE"->Set.of("STAGING_RESTORE","PRODUCTION_RESTORE").contains(e.get("kind"));case "CAPACITY"->"CAPACITY_TEST".equals(e.get("kind"));default->true;};
    }
    static Instant time(Object value){return value instanceof java.time.OffsetDateTime t?t.toInstant():((java.sql.Timestamp)value).toInstant();}
    public Map<String,Object> assessment(UUID id){
        var a=row(id,false);var evidence=evidenceRows(id);var missing=new TreeSet<>(CRITERIA);var risks=new TreeSet<String>();
        for(var e:evidence){if(acceptable(e))missing.remove(e.get("criterion"));else risks.add("UNVERIFIED_"+e.get("criterion"));for(Object risk:(List<?>)e.get("risks"))risks.add(risk.toString());}
        var facts=dependencies.current((String)a.get("service"),((String)a.get("country_code")).trim());
        facts.forEach((key,valid)->{if(!valid)risks.add("CURRENT_"+key+"_MISSING");});
        var n=notice();boolean policy=!n.isEmpty()&&Objects.equals(n.get("version"),a.get("notice_version"))&&hash((String)n.get("content")).equals(a.get("notice_sha256"));
        boolean current=source.matches("[a-f0-9]{40}")&&source.equals(a.get("source_commit"));
        boolean revoked=Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM pilot_assessment_revocations WHERE assessment_id=?)",Boolean.class,id));
        if(!current)risks.add("CURRENT_SOURCE_MISMATCH");if(!policy)risks.add("CURRENT_APPROVED_PILOT_NOTICE_REQUIRED");if(revoked)risks.add("AUTHORITY_OR_ASSESSMENT_REVOKED");
        String state;
        if(evidence.stream().anyMatch(e->"FAIL".equals(e.get("state")))||revoked||risks.stream().anyMatch(r->r.startsWith("CRITICAL_")||r.startsWith("BLOCKING_")))state="NO_GO";
        else if(evidence.isEmpty())state="NOT_ASSESSED";
        else if(!current||facts.containsValue(false)||evidence.stream().anyMatch(e->"BLOCKED".equals(e.get("state"))))state="OPERATIONALLY_BLOCKED";
        else if(!policy||!missing.isEmpty())state=policy&&Collections.disjoint(missing,TECHNICAL)?"TECHNICALLY_READY":"APPROVAL_REQUIRED";
        else state="GO_RECOMMENDED";
        if(state.equals("GO_RECOMMENDED")){
            var approvals=db.queryForList("SELECT owner_id FROM pilot_owner_approvals WHERE assessment_id=? AND assessment_revision=? AND expires_at>CURRENT_TIMESTAMP",id,a.get("revision"));
            if(approvals.stream().anyMatch(o->owners.contains(o.get("owner_id"))&&authority.has((UUID)o.get("owner_id"),"PILOT_OWNER")&&authority.has((UUID)o.get("owner_id"),"TRUST_PLATFORM")))state="OWNER_APPROVED";
        }
        var safe=new LinkedHashMap<String,Object>(a);safe.remove("created_session");return Map.of("assessment",safe,"state",state,"missingCriteria",missing,"remainingRisks",risks,"currentDependencies",facts,"evidence",evidence,"runtimeActivationChanged",false);
    }
    public void approve(UUID who,UUID sid,UUID id,long expected){
        owner(who,sid);var a=row(id,true);revision(a,expected);
        if(who.equals(a.get("created_by"))||evidenceRows(id).stream().anyMatch(e->who.equals(e.get("author_id"))||who.equals(e.get("reviewer_id"))))throw new ForbiddenException("An owner independent of preparation and evidence review must decide");
        if(!"GO_RECOMMENDED".equals(assessment(id).get("state")))throw new ConflictException("All current actual evidence and prerequisites must pass before owner approval");
        db.update("INSERT INTO pilot_owner_approvals(assessment_id,assessment_revision,owner_id,owner_session,expires_at) VALUES(?,?,?,?,CURRENT_TIMESTAMP+INTERVAL '24 hours') ON CONFLICT DO NOTHING",id,expected,who,sid);
        authority.audit(who,id,"PILOT_OWNER_APPROVAL_RECORDED_NO_ACTIVATION");
    }
    public void revoke(UUID who,UUID sid,UUID id){staff(who,sid);row(id,true);db.update("INSERT INTO pilot_assessment_revocations(assessment_id,reason) VALUES(?,'EXPLICIT_STOP') ON CONFLICT DO NOTHING",id);authority.audit(who,id,"PILOT_ASSESSMENT_STOPPED");}
    public Object detail(UUID who,UUID sid,UUID id){staff(who,sid);return assessment(id);}
    public List<Map<String,Object>> list(UUID who,UUID sid){staff(who,sid);return db.queryForList("SELECT id,service,country_code,source_commit,notice_version,revision,created_by,created_at FROM pilot_assessments ORDER BY created_at DESC,id DESC LIMIT 100");}
    public UUID latest(String service,String country){var ids=db.queryForList("SELECT id FROM pilot_assessments WHERE service=? AND country_code=? AND source_commit=? ORDER BY created_at DESC,id DESC LIMIT 1",UUID.class,service,country,source);return ids.isEmpty()?null:ids.getFirst();}
    public Object publicMatrix(String country){if(!country.matches("[A-Z]{2}"))throw new IllegalArgumentException("Country required");return SERVICES.stream().sorted().map(service->{UUID id=latest(service,country);return Map.of("service",service,"assessmentState",id==null?"NOT_ASSESSED":assessment(id).get("state"),"assessmentDoesNotActivateService",true);}).toList();}
}
