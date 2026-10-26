package com.greenocean.backend.identity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.greenocean.backend.preference.repository.PolicyRepository;

@RestController
@RequestMapping("/api/v1/identity")
public class IdentityController {
    private final JdbcTemplate db;
    private final AccessPolicy access;
    private final PolicyRepository policies;
    public IdentityController(JdbcTemplate db,AccessPolicy access,PolicyRepository policies) { this.db=db;this.access=access;this.policies=policies; }
    @GetMapping public Map<String,Object> get(@AuthenticationPrincipal Jwt jwt) {
        UUID id=UUID.fromString(jwt.getSubject());
        var result=new HashMap<String,Object>(db.queryForMap("SELECT status,email_verified,phase_a_enrolled FROM users WHERE id=?",id));
        var eligibility=db.queryForList("SELECT * FROM account_eligibility WHERE user_id=?",id);
        result.put("eligibility",eligibility.isEmpty()?Map.of():eligibility.getFirst());
        var services=new HashMap<String,Boolean>();
        for(String service:List.of("COMMUNITY","LISTENER","PROFESSIONAL_DIRECTORY","PROFESSIONAL_CARE")) {
            try { access.require(id,service); services.put(service,true); } catch(com.greenocean.backend.common.exception.ForbiddenException e) { services.put(service,false); }
        }
        result.put("services",services);
        result.put("terms",policies.latestPublished("terms").orElseThrow());
        return result;
    }
    public record Activation(@NotNull @AssertTrue Boolean adultSelfAttested,@NotBlank String residenceCountry,
        @NotNull Integer termsVersion,String preferredLanguage,String currentCountry,Long cityId,String subdivisionCode) {}
    @PostMapping("/activate") @Transactional
    public Map<String,Object> activate(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Activation request) {
        UUID id=UUID.fromString(jwt.getSubject());
        db.queryForList("SELECT id FROM users WHERE id=? FOR UPDATE",id);
        String country=request.residenceCountry().trim().toUpperCase(Locale.ROOT); access.country(country);
        if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM account_eligibility WHERE user_id=? AND age_method='INDEPENDENT' AND age_result<>'ADULT')",Boolean.class,id)))
            throw new com.greenocean.backend.common.exception.ForbiddenException("An independent eligibility decision requires review");
        if(request.currentCountry()!=null) access.country(request.currentCountry());
        if(request.preferredLanguage()!=null && !request.preferredLanguage().matches("[a-z]{2,3}(-[A-Za-z]{2,8})?")) throw new IllegalArgumentException("Invalid preferred language");
        if(request.cityId()!=null && !Boolean.TRUE.equals(db.queryForObject("""
          SELECT EXISTS(SELECT 1 FROM geography_cities WHERE geonames_id=? AND country_code=? AND (CAST(? AS VARCHAR) IS NULL OR subdivision_code=?))
          """,Boolean.class,request.cityId(),country,request.subdivisionCode(),request.subdivisionCode()))) throw new IllegalArgumentException("City does not belong to the selected country and subdivision");
        if(request.subdivisionCode()!=null && !Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM geography_subdivisions WHERE code=? AND country_code=?)",Boolean.class,request.subdivisionCode(),country))) throw new IllegalArgumentException("Invalid subdivision");
        int version=policies.latestPublished("terms").orElseThrow().version();
        if(request.termsVersion()!=version) throw new com.greenocean.backend.common.exception.RegistrationValidationException("termsVersion", "The Terms have changed. Review and accept the current version.");
        db.update("""
          INSERT INTO account_eligibility(user_id,age_result,age_method,checked_at,residence_country,current_country,preferred_language,city_id)
          VALUES (?,'ADULT','SELF_ATTESTED',CURRENT_TIMESTAMP,?,?,?,?)
          ON CONFLICT(user_id) DO UPDATE SET age_result='ADULT',age_method=CASE WHEN account_eligibility.age_method='INDEPENDENT' THEN 'INDEPENDENT' ELSE 'SELF_ATTESTED' END,
          checked_at=CURRENT_TIMESTAMP,residence_country=EXCLUDED.residence_country,current_country=EXCLUDED.current_country,preferred_language=EXCLUDED.preferred_language,city_id=EXCLUDED.city_id
          """,id,country,request.currentCountry(),request.preferredLanguage(),request.cityId());
        db.update("INSERT INTO policy_acceptances(user_id,policy_key,version) VALUES (?,'terms',?) ON CONFLICT DO NOTHING",id,version);
        db.update("UPDATE users SET verification_activity_at=CURRENT_TIMESTAMP,phase_a_enrolled=TRUE,status=CASE WHEN email_verified THEN 'ACTIVE' ELSE 'PENDING' END WHERE id=? AND status IN ('ACTIVE','PENDING')",id);
        return get(jwt);
    }
    @PostMapping("/display-reviewed") public void displayReviewed(@AuthenticationPrincipal Jwt jwt) {
        db.update("UPDATE account_eligibility SET display_reviewed_at=CURRENT_TIMESTAMP WHERE user_id=?",UUID.fromString(jwt.getSubject()));
    }
}
