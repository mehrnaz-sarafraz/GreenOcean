package com.greenocean.backend;
import com.greenocean.backend.assistance.service.*;
import com.sun.net.httpserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;
import javax.net.ssl.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Real HTTPS capture on loopback; certificate/private key generated at runtime, never committed. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PhaseDMailGatewayTest {
 @TempDir Path directory;HttpsServer server;HttpClient client;String endpoint;
 volatile int responseStatus=202;
 final java.util.List<tools.jackson.databind.JsonNode> received=new CopyOnWriteArrayList<>();
 final Set<String> accepted=ConcurrentHashMap.newKeySet();
 final java.util.List<String> keys=new CopyOnWriteArrayList<>();
 final JsonMapper json=JsonMapper.builder().build();
 @BeforeAll void https()throws Exception{
   Path store=directory.resolve("synthetic-https.p12"),tool=Path.of(System.getProperty("java.home"),"bin",System.getProperty("os.name").startsWith("Windows")?"keytool.exe":"keytool");
   var process=new ProcessBuilder(tool.toString(),"-genkeypair","-alias","qa","-keyalg","RSA","-keysize","2048","-dname","CN=localhost","-ext","SAN=dns:localhost,ip:127.0.0.1","-validity","1","-storetype","PKCS12","-keystore",store.toString(),"-storepass","synthetic-test-only","-keypass","synthetic-test-only","-noprompt").redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();
   assertThat(process.waitFor(20,TimeUnit.SECONDS)).isTrue();assertThat(process.exitValue()).isZero();
   var ks=KeyStore.getInstance("PKCS12");try(var in=Files.newInputStream(store)){ks.load(in,"synthetic-test-only".toCharArray());}
   var km=KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());km.init(ks,"synthetic-test-only".toCharArray());
   var tm=TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());tm.init(ks);
   var ssl=SSLContext.getInstance("TLS");ssl.init(km.getKeyManagers(),tm.getTrustManagers(),null);
   server=HttpsServer.create(new InetSocketAddress("127.0.0.1",0),0);server.setHttpsConfigurator(new HttpsConfigurator(ssl));
   server.createContext("/send",exchange->{
     try{if(!"Bearer synthetic-gateway-credential".equals(exchange.getRequestHeaders().getFirst("Authorization"))){exchange.sendResponseHeaders(401,-1);return;}var body=json.readTree(exchange.getRequestBody());received.add(body);keys.add(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
       if(responseStatus>=200&&responseStatus<300)accepted.add(body.get("deliveryId").asText());
       exchange.getResponseHeaders().set("X-Provider-Reference","qa-provider-reference");exchange.sendResponseHeaders(responseStatus,-1);
     }finally{exchange.close();}
   });server.start();endpoint="https://localhost:"+server.getAddress().getPort()+"/send";
   client=HttpClient.newBuilder().sslContext(ssl).followRedirects(HttpClient.Redirect.NEVER).connectTimeout(java.time.Duration.ofSeconds(3)).build();
 }
 @AfterAll void stop(){if(server!=null)server.stop(0);if(client!=null)client.close();}
 @BeforeEach void reset(){received.clear();accepted.clear();keys.clear();responseStatus=202;}
 WebhookMailTransport gateway(){return new WebhookMailTransport(endpoint,"synthetic-gateway-credential",json,"sender@example.test","staging","allowed@example.test",client);}
 @Test @Tag("happy") void authenticatedHttpsCaptureContainsOnlyReviewedTemplateEnvelopeAndStableIdentifier(){
   UUID id=UUID.randomUUID();var receipt=gateway().submit(id,"sender@example.test","allowed@example.test","email-verification","000123","fa");
   assertThat(receipt.providerReference()).isEqualTo("qa-provider-reference");assertThat(received).hasSize(1);
   var body=received.getFirst();assertThat(body.get("sender").asText()).isEqualTo("sender@example.test");assertThat(body.get("recipient").asText()).isEqualTo("allowed@example.test");assertThat(body.get("language").asText()).isEqualTo("fa");assertThat(body.get("secret").asText()).isEqualTo("000123");assertThat(body.get("templateVersion").asInt()).isEqualTo(1);assertThat(keys).containsExactly(id.toString());
 }
 @Test @Tag("creative") void sameDeliveryIdentifierSupportsProviderIdempotencyAndPasswordResetTemplate(){
   UUID id=UUID.randomUUID();var gateway=gateway();gateway.submit(id,"sender@example.test","allowed@example.test","password-reset","synthetic-reset-token","en");gateway.submit(id,"sender@example.test","allowed@example.test","password-reset","synthetic-reset-token","en");
   assertThat(received).hasSize(2);assertThat(accepted).hasSize(1);assertThat(keys).containsExactly(id.toString(),id.toString());
 }
 @Test @Tag("confused") void temporaryAndPermanentProviderErrorsAreSafeAndDistinct(){
   responseStatus=429;assertThatThrownBy(()->gateway().send(UUID.randomUUID(),"allowed@example.test","email-verification","000123")).isInstanceOfSatisfying(MailDeliveryFailure.class,e->assertThat(e.kind()).isEqualTo(MailDeliveryFailure.Kind.RETRYABLE));
   responseStatus=400;assertThatThrownBy(()->gateway().send(UUID.randomUUID(),"allowed@example.test","email-verification","000123")).isInstanceOfSatisfying(MailDeliveryFailure.class,e->assertThat(e.kind()).isEqualTo(MailDeliveryFailure.Kind.PERMANENT)).hasMessageNotContaining("000123");
 }
 @Test @Tag("malicious") void stagingRefusesUnauthorizedAddressesAndInsecureGateway(){
   assertThatThrownBy(()->gateway().send(UUID.randomUUID(),"not-authorized@example.test","email-verification","000123")).isInstanceOf(MailDeliveryFailure.class);assertThat(received).isEmpty();
   assertThatThrownBy(()->new WebhookMailTransport("http://localhost/send","synthetic",json,"sender@example.test","staging","allowed@example.test",client)).isInstanceOf(IllegalStateException.class);
 }
 @Test @Tag("confused") void transportTimeoutIsUncertainAndNeverLeaksProviderDetails()throws Exception{
   HttpClient timeout=mock(HttpClient.class);doThrow(new HttpTimeoutException("synthetic provider detail 000123")).when(timeout).send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class));
   var gateway=new WebhookMailTransport(endpoint,"synthetic",json,"sender@example.test","staging","allowed@example.test",timeout);
   assertThatThrownBy(()->gateway.send(UUID.randomUUID(),"allowed@example.test","email-verification","000123")).isInstanceOfSatisfying(MailDeliveryFailure.class,e->assertThat(e.kind()).isEqualTo(MailDeliveryFailure.Kind.UNCERTAIN)).hasMessageNotContaining("000123");
 }
}
