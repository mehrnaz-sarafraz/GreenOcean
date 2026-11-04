package com.greenocean.backend;

import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import com.greenocean.backend.professional.service.*;
import com.greenocean.backend.professional.dto.ProfessionalApplicationRequest;
import com.greenocean.backend.listener.planning.*;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/** Owned child JVM used only by phase_q4_process_crashes.py; no production endpoints added. */
public final class PhaseQ4CrashServer {
    public static void main(String[] args)throws Exception {
        String url=System.getenv("GREENOCEAN_TEST_DB_URL"),mode=System.getenv("GREENOCEAN_CRASH_MODE");
        if(!"isolated-only".equals(System.getenv("GREENOCEAN_PROCESS_CRASH_QA"))||url==null||
            !url.matches("jdbc:postgresql://127\\.0\\.0\\.1:55432/greenocean_q4_crash_[a-f0-9]{32}_qa")||
            !Set.of("booking","queue","upload").contains(Objects.toString(mode,"")))
            throw new IllegalStateException("Explicit uniquely named loopback process-crash fixture required");
        Path file=Path.of(System.getenv("GREENOCEAN_CRASH_FIXTURE")).toAbsolutePath().normalize();
        Path objects=Path.of(System.getenv("GREENOCEAN_CRASH_OBJECTS")).toAbsolutePath().normalize();
        if(!file.startsWith(Path.of("target").toAbsolutePath().normalize())||!objects.startsWith(Path.of("target").toAbsolutePath().normalize()))
            throw new IllegalStateException("All crash fixture artifacts must remain under the test target directory");
        var ctx=new SpringApplicationBuilder(BackendApplication.class).profiles("test").run(
            "--server.address=127.0.0.1","--server.port=0","--spring.datasource.url="+url,
            "--spring.datasource.username=postgres","--spring.datasource.password=",
            "--greenocean.clinic.commerce-enabled=true","--greenocean.live-support.enabled=true",
            "--greenocean.listener-support.duration-enabled=true","--greenocean.listener-support.scheduling-enabled=true",
            "--greenocean.listener-support.booking-enabled=true","--greenocean.listener-support.queue-enabled=true",
            "--greenocean.storage.professional-documents-root="+objects);
        var db=ctx.getBean(JdbcTemplate.class);var json=ctx.getBean(ObjectMapper.class);
        Map<String,Object> fixture;
        if(Files.exists(file)){
            fixture=json.readValue(Files.readString(file),new tools.jackson.core.type.TypeReference<LinkedHashMap<String,Object>>(){});
            if(!url.equals(fixture.get("databaseUrl")))throw new IllegalStateException("Fixture database mismatch");
        }else{
            PhaseIFixtures.region(db);
            for(String service:List.of("PROFESSIONAL_CARE","PROFESSIONAL_DIRECTORY"))db.update("INSERT INTO regional_services(country_code,service,status) VALUES('IR',?,'AVAILABLE') ON CONFLICT(country_code,service) DO UPDATE SET status='AVAILABLE'",service);
            var users=ctx.getBean(UserRepository.class);var tokens=ctx.getBean(TokenService.class);
            var member=PhaseIFixtures.member(db,users,tokens);var peer=PhaseIFixtures.member(db,users,tokens);var staff=PhaseIFixtures.member(db,users,tokens);
            fixture=new LinkedHashMap<>();fixture.put("databaseUrl",url);fixture.put("token",member.token());fixture.put("userId",member.id());fixture.put("mode",mode);
            if(mode.equals("booking")){
                PhasePFixtures.qualified(db,peer.id(),staff.id());var commerce=ctx.getBean(ClinicCommerce.class);
                UUID policy=commerce.policy(staff.id(),new ClinicCommerce.Policy("q4-crash-"+UUID.randomUUID(),0,0,"Synthetic process-crash booking terms","Synthetic process-crash refund and cancellation terms"));
                UUID slot=ctx.getBean(ProfessionalScheduling.class).slot(peer.id(),new ProfessionalScheduling.Slot(Instant.now().plusSeconds(7200),Instant.now().plusSeconds(9000),"TEXT",0,"UTC",true));
                commerce.offer(peer.id(),slot,policy,false);fixture.put("path","/api/v1/clinic/appointments");
                fixture.put("body",new ClinicCommerce.Booking(slot,UUID.randomUUID(),2,policy,0,true));fixture.put("table","consultation_requests");
            }else if(mode.equals("queue")){
                PhaseIFixtures.review(db,peer.id(),staff.id(),true);var policy=ctx.getBean(ListenerSupportPolicy.class);Instant start=policy.now().plusSeconds(7200);
                ctx.getBean(ListenerCalendar.class).exception(peer.id(),new ListenerCalendar.ExceptionInput("UTC",start.atOffset(ZoneOffset.UTC).toLocalDateTime().toString(),start.plusSeconds(14400).atOffset(ZoneOffset.UTC).toLocalDateTime().toString(),null,null,true));
                ctx.getBean(ListenerQueueCoverage.class).approve(staff.id(),new ListenerQueueCoverage.Window(peer.id(),policy.now().minusSeconds(60),policy.now().plusSeconds(14400)));
                fixture.put("listenerToken",peer.token());fixture.put("path","/api/v1/listener-support/queue");fixture.put("table","listener_wait_queue");
                fixture.put("body",new ListenerQueue.Admission(UUID.randomUUID(),null,false));
            }else{
                UUID application=ctx.getBean(ProfessionalWorkflowService.class).apply(member.id(),new ProfessionalApplicationRequest(
                    "PSYCHOLOGIST","Synthetic application","Synthetic specialty","Synthetic original process-crash application",2,List.of("General support"),List.of("فارسی"),null,"IR",null,null,null,null,List.of("Synthetic degree"),"QA-CRASH",List.of("TEXT"),List.of("credential.pdf"))).id();
                fixture.put("path","/api/v1/professional-applications/"+application+"/documents");fixture.put("table","professional_application_documents");fixture.put("body",Map.of());
            }
        }
        fixture.put("baseUrl","http://127.0.0.1:"+ctx.getEnvironment().getRequiredProperty("local.server.port"));
        Files.writeString(file,json.writeValueAsString(fixture));
        try{Files.setPosixFilePermissions(file,java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));}catch(UnsupportedOperationException ignored){}
        System.out.println("Q4 disposable "+mode+" child ready");new CountDownLatch(1).await();
    }
}
