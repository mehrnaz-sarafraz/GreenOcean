package com.greenocean.backend.trust;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import com.greenocean.backend.professional.service.*;
import java.util.*;

@Component
public class CurrentPilotDependencies implements PilotDependencyChecks {
    private final JdbcTemplate db;private final TrustCoverage coverage;private final OperationsAlertGateway alerts;
    private final ObjectProvider<ClinicPaymentProvider> payments;private final ObjectProvider<ClinicalMediaProvider> media;
    private final boolean mail,alertWorker;private final String keys;
    public CurrentPilotDependencies(JdbcTemplate db,TrustCoverage coverage,OperationsAlertGateway alerts,ObjectProvider<ClinicPaymentProvider> payments,ObjectProvider<ClinicalMediaProvider> media,
        org.springframework.core.env.Environment environment,@Value("${greenocean.mail.enabled:false}")boolean mail,@Value("${greenocean.clinic.record-keys:}")String keys){this.db=db;this.coverage=coverage;this.alerts=alerts;this.payments=payments;this.media=media;this.mail=mail&&environment.matchesProfiles("prod")&&"production".equals(environment.getProperty("greenocean.mail.mode"));this.alertWorker=environment.getProperty("greenocean.operations.enabled",Boolean.class,false)&&environment.getProperty("greenocean.operations.alert-worker-enabled",Boolean.class,false);this.keys=keys;}
    static boolean real(Object provider){String name=provider.getClass().getName().toLowerCase(Locale.ROOT);return !name.contains("synthetic")&&!name.contains("test")&&!name.contains("fake");}
    public Map<String,Boolean> current(String service,String country){
        String domain=PilotReadiness.domain(service),regional=service.equals("LISTENERS")?"LISTENER":service.startsWith("CLINICAL")||Set.of("PROFESSIONAL_BOOKING","REAL_PAYMENTS").contains(service)?"PROFESSIONAL_CARE":"COMMUNITY";
        boolean provider=switch(service){case "REAL_PAYMENTS"->payments.stream().anyMatch(p->real(p)&&p.authorized());case "CLINICAL_MEDIA"->media.stream().anyMatch(p->real(p)&&p.authorized(country,"AUDIO")&&p.authorized(country,"VIDEO"));case "COMMUNITY"->mail;default->true;};
        boolean routes=alertWorker&&alerts.configured("PRIMARY")&&alerts.configured("ESCALATION");
        for(String destination:List.of("PRIMARY","ESCALATION"))routes&=Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM operation_alert_routes r WHERE r.domain=? AND r.destination_key=? AND r.approved_by<>r.author_id AND "+OperationsAlerts.CURRENT_ROUTE+")",Boolean.class,domain,destination));
        return Map.of("STAFFING",Boolean.TRUE.equals(coverage.availability(domain).get("available")),"MONITORING",routes,"PROVIDERS",provider,
            "REGION",Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM regional_services WHERE country_code=? AND service=? AND status='AVAILABLE')",Boolean.class,country,regional)),
            "KEYS",!service.startsWith("CLINICAL")||!keys.isBlank());
    }
}
