package com.greenocean.backend.identity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class PublicCatalogController {
    private final JdbcTemplate db;
    private final AccessPolicy access;
    public PublicCatalogController(JdbcTemplate db,AccessPolicy access) { this.db=db;this.access=access; }
    @GetMapping("/geography/countries") public List<Map<String,Object>> countries(@RequestParam(defaultValue="") String q) {
        if(q.length()>100) throw new IllegalArgumentException("Search is too long");
        return db.queryForList("SELECT code,name,local_names,catalog_version FROM geography_countries WHERE name ILIKE ? OR code ILIKE ? OR local_names::text ILIKE ? ORDER BY name LIMIT 250","%"+q+"%","%"+q+"%","%"+q+"%");
    }
    @GetMapping("/geography/subdivisions") public List<Map<String,Object>> subdivisions(@RequestParam String country) {
        access.country(country); return db.queryForList("SELECT code,name,local_names FROM geography_subdivisions WHERE country_code=? ORDER BY name",country);
    }
    @GetMapping("/geography/cities") public List<Map<String,Object>> cities(@RequestParam String country,@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="") String subdivision) {
        access.country(country);
        if(q.length()>100) throw new IllegalArgumentException("Search is too long");
        return db.queryForList("SELECT geonames_id,name,subdivision_code,local_names FROM geography_cities WHERE country_code=? AND (?='' OR subdivision_code=?) AND (name ILIKE ? OR local_names::text ILIKE ?) ORDER BY name LIMIT 50",country,subdivision,subdivision,"%"+q+"%","%"+q+"%");
    }
    @GetMapping("/geography/services") public List<Map<String,Object>> services(@RequestParam String country) {
        access.country(country);
        return List.of("COMMUNITY","LISTENER","PROFESSIONAL_DIRECTORY","PROFESSIONAL_CARE","URGENT_RESOURCES").stream()
          .map(service -> db.queryForList("SELECT service,status,reason FROM regional_services WHERE country_code=? AND service=?",country,service)
          .stream().findFirst().orElse(Map.of("service",service,"status","UNDER_REVIEW","reason","Service has not been enabled for this country"))).toList();
    }
    @GetMapping("/public/resources") public Map<String,Object> resources() {
        // Deliberately separate from community posts, private profiles, and professional articles.
        return Map.of("audience","ALL_AGES","items",List.of(
          Map.of("id","grounding","title","Pause and notice your surroundings","body","If comfortable, notice five things you can see and feel your feet on the ground. You can stop at any time."),
          Map.of("id","trusted-person","title","Talk with someone you trust","body","You can ask a trusted adult or someone supportive to stay with you while you find help.")),
          "boundaries",List.of("Community members share lived experiences.","Trained listeners provide non-clinical support.","Professional care requires verified qualifications and regional availability.","GreenOcean is not an emergency service and does not promise immediate responses."));
    }
}
