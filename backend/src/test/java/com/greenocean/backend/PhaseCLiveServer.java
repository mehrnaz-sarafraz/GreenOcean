package com.greenocean.backend;

import com.greenocean.backend.assistance.service.MailTransport;
import com.sun.net.httpserver.HttpServer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.ObjectMapper;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Explicitly opt-in CI server. Test classes are not part of the production application jar. */
public class PhaseCLiveServer {
    static final Map<String,String> delivered=new ConcurrentHashMap<>();
    @TestConfiguration(proxyBeanMethods=false)
    static class TestMail {
        @Bean @Primary MailTransport isolatedDelivery() {
            return (id,to,template,secret)->{
                if(!to.endsWith("@example.test"))throw new IllegalArgumentException("Synthetic recipients only");
                if(template.equals("email-verification"))delivered.put(to,secret);
            };
        }
    }
    public static void main(String[] args) throws Exception {
        if(!"isolated-only".equals(System.getenv("GREENOCEAN_LIVE_BROWSER_QA")) ||
           System.getenv("GREENOCEAN_TEST_DB_URL")==null)
            throw new IllegalStateException("An explicitly isolated test database and opt-in are required");
        var context=new SpringApplicationBuilder(BackendApplication.class,TestMail.class).profiles("test").run(
            "--server.port=8080","--server.address=127.0.0.1","--greenocean.mail.enabled=true",
            "--greenocean.mail.poll-ms=200","--greenocean.auth.web-cookie-secure=false",
            "--greenocean.security.allowed-origin-patterns=http://127.0.0.1:8081");
        var db=context.getBean(JdbcTemplate.class);var json=context.getBean(ObjectMapper.class);
        // Only this disposable QA database is given a synthetic available region.
        db.update("INSERT INTO regional_services(country_code,service,status) VALUES ('IR','COMMUNITY','AVAILABLE') ON CONFLICT(country_code,service) DO UPDATE SET status='AVAILABLE'");
        String key=UUID.randomUUID().toString()+UUID.randomUUID();
        Path token=Path.of("target","qa-mailbox-token");Files.writeString(token,key);
        try {Files.setPosixFilePermissions(token,java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));}
        catch(UnsupportedOperationException ignored) { /* Windows development files inherit workspace ACL. */ }
        var mailbox=HttpServer.create(new InetSocketAddress("127.0.0.1",8099),0);
        mailbox.createContext("/snapshot",exchange->{
            try {
                if(!"GET".equals(exchange.getRequestMethod()) || !key.equals(exchange.getRequestHeaders().getFirst("X-QA-Key"))) {
                    exchange.sendResponseHeaders(403,-1);return;
                }
                String query=exchange.getRequestURI().getRawQuery();
                String email=query!=null&&query.startsWith("email=")?URLDecoder.decode(query.substring(6),StandardCharsets.UTF_8).toLowerCase(Locale.ROOT):"";
                if(!email.endsWith("@example.test")){exchange.sendResponseHeaders(400,-1);return;}
                var data=new LinkedHashMap<String,Object>();
                data.put("code",delivered.get(email));
                data.put("accounts",db.queryForObject("SELECT count(*) FROM users WHERE lower(email)=lower(?)",Integer.class,email));
                var rows=db.queryForList("SELECT u.status,u.email_verified,p.username,p.display_name,p.city,p.birth_year,p.is_profile_private,u.password_hash LIKE '{pbkdf2}%' AS modern_hash FROM users u JOIN profiles p ON p.user_id=u.id WHERE u.email=?",email);
                data.put("account",rows.isEmpty()?null:rows.getFirst());
                data.put("roles",db.queryForList("SELECT r.name FROM roles r JOIN user_roles ur ON ur.role_id=r.id JOIN users u ON u.id=ur.user_id WHERE u.email=?",String.class,email));
                data.put("acceptances",db.queryForObject("SELECT count(*) FROM policy_acceptances a JOIN users u ON u.id=a.user_id WHERE u.email=?",Integer.class,email));
                byte[] body=json.writeValueAsBytes(data);exchange.getResponseHeaders().set("Content-Type","application/json");
                exchange.sendResponseHeaders(200,body.length);exchange.getResponseBody().write(body);
            } finally {exchange.close();}
        });
        mailbox.start();Runtime.getRuntime().addShutdownHook(new Thread(()->{mailbox.stop(0);context.close();}));
    }
}
