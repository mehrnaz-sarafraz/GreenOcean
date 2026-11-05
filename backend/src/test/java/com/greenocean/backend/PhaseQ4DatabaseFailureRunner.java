package com.greenocean.backend;

import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Physical failure injection into the explicitly owned disposable loopback cluster only. */
public final class PhaseQ4DatabaseFailureRunner {
    public static void main(String[] args)throws Exception {
        String url=System.getenv("GREENOCEAN_TEST_DB_URL");
        if(!"isolated-only".equals(System.getenv("GREENOCEAN_DATABASE_FAILURE_QA"))||
            !"jdbc:postgresql://127.0.0.1:55432/greenocean_q4_load_qa".equals(url))
            throw new IllegalStateException("Explicit isolated database-failure opt-in required");
        Path root=Path.of("..").toRealPath();
        Path data=root.resolve(".tmp/phase-l-pg").toRealPath();
        Path control=root.resolve(".tmp/tools/postgres/pgsql/bin/pg_ctl.exe").toRealPath();
        if(!Files.readString(data.resolve("PG_VERSION")).trim().equals("18"))throw new IllegalStateException("Expected owned PostgreSQL18 fixture");
        boolean stopped=false;
        try(var ctx=new SpringApplicationBuilder(BackendApplication.class).profiles("test").run(
            "--server.address=127.0.0.1","--server.port=0","--spring.datasource.url="+url,
            "--spring.datasource.username=postgres","--spring.datasource.password=",
            "--spring.datasource.hikari.connection-timeout=2000")) {
            var db=ctx.getBean(JdbcTemplate.class);PhaseIFixtures.region(db);
            var actor=PhaseIFixtures.member(db,ctx.getBean(UserRepository.class),ctx.getBean(TokenService.class));
            var writing=ctx.getBean(com.greenocean.backend.wellbeing.service.PrivateWellbeing.class);
            writing.write(actor.id(),null,new com.greenocean.backend.wellbeing.service.PrivateWellbeing.Writing(
                "Synthetic restart marker","A private synthetic entry acknowledged before database restart","",List.of(),0,UUID.randomUUID()));
            long before=db.queryForObject("SELECT count(*) FROM wellbeing_journal_entries WHERE user_id=?",Long.class,actor.id());
            String base="http://127.0.0.1:"+ctx.getEnvironment().getRequiredProperty("local.server.port");
            var client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
            var request=HttpRequest.newBuilder(URI.create(base+"/api/v1/wellbeing/journal")).timeout(Duration.ofSeconds(8))
                .header("Authorization","Bearer "+actor.token()).GET().build();
            int healthy=client.send(request,HttpResponse.BodyHandlers.discarding()).statusCode();
            if(healthy!=200)throw new AssertionError("Pre-failure authenticated private read was not healthy");
            control(control,data,"stop");stopped=true;long start=System.nanoTime();
            int unavailable=client.send(request,HttpResponse.BodyHandlers.discarding()).statusCode();
            if(unavailable!=503)throw new AssertionError("Database outage must fail closed with truthful temporary unavailability; received "+unavailable);
            control(control,data,"start");stopped=false;
            int recovered=0,attempts=0;
            while(System.nanoTime()-start<TimeUnit.SECONDS.toNanos(45)){
                attempts++;recovered=client.send(request,HttpResponse.BodyHandlers.discarding()).statusCode();
                if(recovered==200)break;Thread.sleep(500);
            }
            double elapsed=(System.nanoTime()-start)/1e9;
            if(recovered!=200)throw new AssertionError("Private read did not recover within the test bound");
            long after=db.queryForObject("SELECT count(*) FROM wellbeing_journal_entries WHERE user_id=?",Long.class,actor.id());
            if(before!=after)throw new AssertionError("Acknowledged synthetic entry changed during restart");
            var result=Map.of("evidenceKind","SYNTHETIC_LOCAL_PHYSICAL_DATABASE_RESTART",
                "healthyStatus",healthy,"outageStatus",unavailable,"recoveredStatus",recovered,
                "interruptionToPrivateReadSeconds",elapsed,"recoveryAttempts",attempts,"acknowledgedEntryPreserved",true,
                "limitations",List.of("Single local database restart","One authenticated private-read service probe","Not an in-flight booking, queue or upload process-crash drill","No production RTO commitment"));
            Files.writeString(Path.of("target","phase-q4-database-restart.json"),ctx.getBean(ObjectMapper.class).writerWithDefaultPrettyPrinter().writeValueAsString(result));
            System.out.println("Q4 physical database restart verified; private-read recovery seconds="+elapsed);
        }finally{if(stopped)control(control,data,"start");}
    }
    static void control(Path executable,Path data,String action)throws Exception {
        var args=new ArrayList<>(List.of(executable.toString(),"-D",data.toString(),"-w","-t","20"));
        if(action.equals("start"))args.addAll(List.of("-l",data.resolve("server.log").toString(),"-o","-p 55432 -h 127.0.0.1 -c max_connections=100"));
        else args.addAll(List.of("-m","fast"));
        args.add(action);
        var process=new ProcessBuilder(args).redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.appendTo(Path.of("target","phase-q4-database-control.log").toFile())).start();
        if(!process.waitFor(25,TimeUnit.SECONDS)||process.exitValue()!=0)throw new IllegalStateException("Owned QA database "+action+" failed");
    }
}
