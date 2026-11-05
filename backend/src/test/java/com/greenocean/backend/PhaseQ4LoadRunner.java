package com.greenocean.backend;

import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.service.TokenService;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.builder.SpringApplicationBuilder;

import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.ObjectMapper;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/** Explicit standalone QA exercise, never loaded into the production jar or normal test suite. */
public final class PhaseQ4LoadRunner {
    record Work(String name,String method,String path,String body) {}
    record Sample(String name,int status,long micros,String outcome) {}
    static final List<Work> READS=List.of(
        new Work("feed","GET","/api/v1/posts/discovery?mode=DISCOVER&size=20",null),
        new Work("groups","GET","/api/v1/communities",null),
        new Work("private-journal","GET","/api/v1/wellbeing/journal",null),
        new Work("mood-history","GET","/api/v1/wellbeing/check-ins",null),
        new Work("growth-library","GET","/api/v1/growth-library/resources",null),
        new Work("lessons","GET","/api/v1/wellbeing/courses",null),
        new Work("listener-schedules","GET","/api/v1/listener-support/slots",null),
        new Work("listener-queue","GET","/api/v1/listener-support/queue/me",null),
        new Work("professional-directory","GET","/api/v1/professionals",null));

    public static void main(String[] args)throws Exception {
        String url=System.getenv("GREENOCEAN_TEST_DB_URL");
        if(!"isolated-only".equals(System.getenv("GREENOCEAN_LOAD_QA"))||url==null||!url.matches("jdbc:postgresql://127\\.0\\.0\\.1:55432/greenocean_q4_load_qa"))
            throw new IllegalStateException("Explicit disposable loopback load database required");
        int duration=Integer.parseInt(System.getenv().getOrDefault("GREENOCEAN_LOAD_SECONDS","20"));
        if(duration<10||duration>120)throw new IllegalArgumentException("Measured duration must be 10..120 seconds");
        try(var ctx=new SpringApplicationBuilder(BackendApplication.class,PhaseP2PaymentsTest.Providers.class).profiles("test").run(
            "--server.address=127.0.0.1","--server.port=0","--spring.datasource.url="+url,
            "--spring.datasource.username=postgres","--spring.datasource.password=",
            "--greenocean.listener-support.scheduling-enabled=true","--greenocean.listener-support.queue-enabled=true",
            "--greenocean.listener-support.duration-enabled=true","--greenocean.live-support.enabled=true",
            "--greenocean.listener-support.booking-enabled=true",
            "--greenocean.trust.enabled=true","--greenocean.clinic.commerce-enabled=true","--greenocean.clinic.records-enabled=true",
            "--greenocean.clinic.record-keys=clinical-qa:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","--greenocean.clinic.record-current-key=clinical-qa",
            "--greenocean.wellbeing.new-sensitive-processing=true")) {
            var db=ctx.getBean(JdbcTemplate.class);var users=ctx.getBean(UserRepository.class);var tokens=ctx.getBean(TokenService.class);
            var json=ctx.getBean(ObjectMapper.class);PhaseIFixtures.region(db);
            db.update("INSERT INTO regional_services(country_code,service,status) VALUES('IR','PROFESSIONAL_DIRECTORY','AVAILABLE') ON CONFLICT(country_code,service) DO UPDATE SET status='AVAILABLE'");
            var actors=new ArrayList<PhaseIFixtures.Actor>();
            for(UUID existing:db.queryForList("SELECT u.id FROM users u WHERE email LIKE 'i-%@example.test' AND status='ACTIVE' AND NOT EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=u.id AND r.name<>'USER') ORDER BY created_at DESC,u.id LIMIT 100",UUID.class))
                actors.add(PhaseIFixtures.session(db,users,tokens,existing));
            int newlyCreated=100-actors.size();
            while(actors.size()<100)actors.add(PhaseIFixtures.member(db,users,tokens));
            var writing=ctx.getBean(com.greenocean.backend.wellbeing.service.PrivateWellbeing.class);
            var posts=ctx.getBean(com.greenocean.backend.post.service.PostService.class);
            UUID commentTarget=null;
            for(var actor:actors){
                if(db.queryForObject("SELECT count(*) FROM wellbeing_journal_entries WHERE user_id=?",Long.class,actor.id())==0)
                    writing.write(actor.id(),null,new com.greenocean.backend.wellbeing.service.PrivateWellbeing.Writing(
                    "Synthetic load entry","A synthetic private reflection for isolated capacity testing","CALM",List.of(),0,UUID.randomUUID()));
                if(db.queryForObject("SELECT count(*) FROM wellbeing_check_ins WHERE user_id=?",Long.class,actor.id())==0)
                    writing.mood(actor.id(),null,new com.greenocean.backend.wellbeing.service.PrivateWellbeing.Mood("CALM","Synthetic local observation",0,UUID.randomUUID()));
                var existingPosts=db.queryForList("SELECT id FROM posts WHERE author_id=? AND status='PUBLISHED' LIMIT 1",UUID.class,actor.id());
                UUID postId=existingPosts.isEmpty()?posts.create(actor.id(),new com.greenocean.backend.post.dto.CreatePostRequest(
                    "A synthetic shared reflection for isolated capacity testing",true,
                    com.greenocean.backend.post.entity.PostVisibility.PUBLIC,null,null,null,null,null,true,"ANONYMOUS")).id():existingPosts.getFirst();
                if(commentTarget==null)commentTarget=postId;
            }
            var works=new ArrayList<Work>(READS);works.addAll(READS); // Explicit read-heavy distribution.
            works.add(new Work("anonymous-post","POST","/api/v1/posts","{\"body\":\"A synthetic reflection for isolated load testing LOAD_NONCE\",\"anonymous\":true,\"publicationIdentity\":\"ANONYMOUS\",\"visibility\":\"PUBLIC\",\"broaderAudienceConfirmed\":true}"));
            works.add(new Work("comment","POST","/api/v1/posts/"+commentTarget+"/comments","{\"body\":\"A synthetic supportive response for isolated load testing LOAD_NONCE\",\"anonymous\":true}"));
            works.add(new Work("journal-write","POST","/api/v1/wellbeing/journal/entries","{\"title\":\"Synthetic load entry\",\"body\":\"A private synthetic load reflection\",\"tags\":[],\"revision\":0,\"clientKey\":\"CLIENT_KEY\"}"));
            works.add(new Work("mood-write","POST","/api/v1/wellbeing/check-ins/entries","{\"mood\":\"CALM\",\"note\":\"Synthetic local observation\",\"revision\":0,\"clientKey\":\"CLIENT_KEY\"}"));
            var fixtures=new PhaseQ4MixedFixtures(ctx,actors);
            String base="http://127.0.0.1:"+ctx.getEnvironment().getRequiredProperty("local.server.port");
            var client=HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(3)).build();
            for(var read:READS){
                int status=client.send(HttpRequest.newBuilder(URI.create(base+read.path())).timeout(Duration.ofSeconds(10))
                    .header("Authorization","Bearer "+actors.getFirst().token()).GET().build(),HttpResponse.BodyHandlers.discarding()).statusCode();
                if(status!=200)throw new IllegalStateException("Successful-read preflight failed for "+read.name()+": "+status);
            }
            var runs=new ArrayList<Object>();
            for(int concurrency:List.of(25,50,75,100))for(int repetition=1;repetition<=2;repetition++) {
                var assigned=fixtures.round(works,runs.size());
                var warmup=assigned.stream().map(list->list.stream().map(work->
                    Set.of("appointment-booking","payment-callback","registration","login").contains(work.name())?
                        new Work("warmup-read","GET","/api/v1/clinic/appointments",null):work).toList()).toList();
                exercise(client,base,actors,warmup,concurrency,5);
                var before=metrics(ctx,db);long start=System.nanoTime();
                var observations=new ConcurrentLinkedQueue<Object>();var sampler=Executors.newSingleThreadScheduledExecutor();
                sampler.scheduleWithFixedDelay(()->{try{observations.add(metrics(ctx,db));}catch(RuntimeException unavailable){observations.add(Map.of("sampleUnavailable",true));}},0,1,TimeUnit.SECONDS);
                List<Sample> samples;
                try{samples=exercise(client,base,actors,assigned,concurrency,duration);}
                finally{sampler.shutdownNow();sampler.awaitTermination(5,TimeUnit.SECONDS);}
                double elapsed=(System.nanoTime()-start)/1e9;
                var report=new LinkedHashMap<String,Object>();
                report.put("concurrentUsers",concurrency);report.put("repetition",repetition);
                report.put("warmupSeconds",5);report.put("configuredSeconds",duration);report.put("actualSeconds",elapsed);
                report.put("requests",samples.size());report.put("throughputPerSecond",samples.size()/elapsed);
                report.put("latency",percentiles(samples));
                report.put("successfulRequestLatency",percentiles(samples.stream().filter(s->s.status()>=200&&s.status()<300).toList()));
                long unexpected=samples.stream().filter(s->s.outcome().equals("UNEXPECTED")).count();
                report.put("unexpectedFailures",unexpected);report.put("unexpectedFailureFraction",unexpected/(double)samples.size());
                report.put("protective429Count",samples.stream().filter(s->s.status()==429).count());
                report.put("publishingProtective409Count",samples.stream().filter(s->s.outcome().equals("PUBLISHING_PROTECTION")).count());
                var perWork=new LinkedHashMap<String,Object>();
                samples.stream().map(Sample::name).distinct().sorted().forEach(name->{var subset=samples.stream().filter(s->s.name().equals(name)).toList();
                    var stats=new LinkedHashMap<String,Object>(percentiles(subset));
                    stats.put("statusCounts",subset.stream().collect(java.util.stream.Collectors.groupingBy(Sample::status,TreeMap::new,java.util.stream.Collectors.counting())));
                    perWork.put(name,stats);});
                report.put("workloads",perWork);report.put("metricsBefore",before);report.put("resourceObservations",List.copyOf(observations));report.put("metricsAfter",metrics(ctx,db));runs.add(report);
                System.out.println("Q4 measured users="+concurrency+" repetition="+repetition+" requests="+samples.size());
                // Persist after each run so interruption cannot be mistaken for a completed series.
                Files.writeString(Path.of("target","phase-q4-load-results.json"),json.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of(
                    "evidenceKind","SYNTHETIC_LOCAL_HTTP","completedRuns",runs,"logicalProcessors",Runtime.getRuntime().availableProcessors(),
                    "os",System.getProperty("os.name"),"javaVersion",System.getProperty("java.version"),"dataset",Map.of("activeSyntheticClients",100,"newSyntheticAccounts",newlyCreated,"totalUsers",db.queryForObject("SELECT count(*) FROM users",Long.class),"totalPosts",db.queryForObject("SELECT count(*) FROM posts",Long.class),"totalPrivateEntries",db.queryForObject("SELECT count(*) FROM wellbeing_journal_entries",Long.class),"totalMoodObservations",db.queryForObject("SELECT count(*) FROM wellbeing_check_ins",Long.class)),
                    "postgresSettings",Map.of("sharedBuffers",db.queryForObject("SHOW shared_buffers",String.class),"maxConnections",db.queryForObject("SHOW max_connections",String.class)),
                    "limitations",List.of("Loopback client and server share CPU in one JVM","Synthetic professional credentials, purpose operators and clinical notices; no real care or provider activation","Ten appointment/payment participants, one clinical signaling participant and one staff participant within each concurrent cohort","Catalogue reads may have no reviewed synthetic course/resource content; inspect dataset separately","Preserved publishing protections can return verified 409 and 429 responses","No production capacity guarantee","One-second resource observations can miss shorter peaks; worker delivery is not exercised by this mix"))));
                if(unexpected>0)throw new IllegalStateException("Measured run has unexpected responses; inspect the aggregate report before continuing");
            }
        }
    }
    static List<Sample> exercise(HttpClient client,String base,List<PhaseIFixtures.Actor> actors,List<List<Work>> assigned,int n,int seconds)throws Exception {
        var output=new ConcurrentLinkedQueue<Sample>();long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(seconds);
        try(var pool=Executors.newFixedThreadPool(n)) {
            var tasks=new ArrayList<Callable<Void>>();
            for(int i=0;i<n;i++){final int index=i;tasks.add(()->{int step=index;var works=assigned.get(index);while(System.nanoTime()<end){
                Work work=works.get(step++%works.size());long start=System.nanoTime();int status;String outcome="UNEXPECTED";
                try{var builder=HttpRequest.newBuilder(URI.create(base+work.path())).timeout(Duration.ofSeconds(10));
                    if(!Set.of("registration","login","payment-callback").contains(work.name()))builder.header("Authorization","Bearer "+actors.get(index).token());
                    builder.header("Content-Type",work.name().equals("payment-callback")?"application/octet-stream":"application/json");
                    if(work.name().equals("payment-callback"))builder.header("X-Provider-Signature","SYNTHETIC-VALID-SIGNATURE");
                    var request=builder.method(work.method(),work.body()==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(work.body().replace("CLIENT_KEY",UUID.randomUUID().toString()).replace("LOAD_NONCE",UUID.randomUUID().toString()))).build();
                    var response=client.send(request,HttpResponse.BodyHandlers.ofString());status=response.statusCode();
                    if(status>=200&&status<300)outcome="SUCCESS";
                    else if(status==429)outcome="RATE_PROTECTION";
                    else if(status==409&&Set.of("anonymous-post","comment").contains(work.name())){
                        // Match only the two fixed publishing guard messages; never export response bodies.
                        var body=new ObjectMapper().readTree(response.body());String message=body.path("message").asText();
                        if(Set.of("Please pause before submitting more content","This submission was already received; check your content before retrying").contains(message))outcome="PUBLISHING_PROTECTION";
                    }
                }catch(Exception e){status=0;}
                output.add(new Sample(work.name(),status,TimeUnit.NANOSECONDS.toMicros(System.nanoTime()-start),outcome));
                Thread.sleep(250);
            }return null;});}for(var task:pool.invokeAll(tasks))task.get();
        }return List.copyOf(output);
    }
    static Map<String,Object> percentiles(List<Sample> samples){
        long[] sorted=samples.stream().mapToLong(Sample::micros).sorted().toArray();
        var result=new LinkedHashMap<String,Object>();result.put("samples",sorted.length);
        for(int percentile:List.of(50,95,99))if(sorted.length>=(percentile==99?10000:20))result.put("p"+percentile+"Ms",sorted[(int)Math.ceil(percentile/100.0*sorted.length)-1]/1000.0);
        result.put("p99MinimumSamples",10000);return result;
    }
    static Map<String,Object> metrics(org.springframework.context.ApplicationContext ctx,JdbcTemplate db){
        var result=new LinkedHashMap<String,Object>();var runtime=Runtime.getRuntime();
        result.put("heapUsedBytes",runtime.totalMemory()-runtime.freeMemory());result.put("heapMaxBytes",runtime.maxMemory());
        var os=(com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
        result.put("processCpuSeconds",os.getProcessCpuTime()/1e9);result.put("processCpuLoad",os.getProcessCpuLoad());
        result.put("gcCount",ManagementFactory.getGarbageCollectorMXBeans().stream().mapToLong(b->Math.max(0,b.getCollectionCount())).sum());
        result.put("gcMillis",ManagementFactory.getGarbageCollectorMXBeans().stream().mapToLong(b->Math.max(0,b.getCollectionTime())).sum());
        var pool=ctx.getBean(HikariDataSource.class).getHikariPoolMXBean();
        result.put("pool",Map.of("active",pool.getActiveConnections(),"idle",pool.getIdleConnections(),"waiting",pool.getThreadsAwaitingConnection()));
        result.put("database",db.queryForMap("SELECT xact_commit,xact_rollback,deadlocks FROM pg_stat_database WHERE datname=current_database()"));
        result.put("lockWaiters",db.queryForObject("SELECT count(*) FROM pg_stat_activity WHERE datname=current_database() AND wait_event_type='Lock'",Long.class));
        result.put("waitingQueueDepth",db.queryForObject("SELECT count(*) FROM listener_wait_queue WHERE status IN('WAITING','OFFERED')",Long.class));
        result.put("alertBacklog",db.queryForObject("SELECT count(*) FROM operation_alerts WHERE state IN('QUEUED','CLAIMED')",Long.class));
        return result;
    }
}
