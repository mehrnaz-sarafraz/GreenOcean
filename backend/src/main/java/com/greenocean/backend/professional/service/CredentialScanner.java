package com.greenocean.backend.professional.service;
import org.springframework.stereotype.Component;import org.springframework.beans.factory.annotation.Value;import java.net.*;import java.io.*;
/** Genuine ClamAV INSTREAM adapter; absent approval/daemon means quarantine, never clean-by-default. */
@Component public class CredentialScanner {
 private final boolean approved;private final String host;private final int port;
 public record Result(String state,String reference){}
 public CredentialScanner(@Value("${greenocean.clinic.scanning-approved:false}")boolean approved,@Value("${greenocean.clinic.scanner-host:127.0.0.1}")String host,@Value("${greenocean.clinic.scanner-port:3310}")int port){this.approved=approved;this.host=host;this.port=port;}
 public Result scan(byte[] bytes){if(!approved)return new Result("PENDING",null);try(var socket=new Socket()){socket.connect(new InetSocketAddress(host,port),2000);socket.setSoTimeout(5000);var out=new DataOutputStream(socket.getOutputStream());out.writeBytes("zINSTREAM\0");for(int i=0;i<bytes.length;i+=8192){int n=Math.min(8192,bytes.length-i);out.writeInt(n);out.write(bytes,i,n);}out.writeInt(0);out.flush();var response=new ByteArrayOutputStream();for(int b;(b=socket.getInputStream().read())>0;){if(response.size()>512)throw new IOException("Invalid scanner response");response.write(b);}String result=response.toString(java.nio.charset.StandardCharsets.US_ASCII);if(result.equals("stream: OK"))return new Result("CLEAN","CLAMAV-INSTREAM");if(result.endsWith(" FOUND"))return new Result("REJECTED","CLAMAV-INSTREAM");return new Result("PENDING",null);}catch(IOException unavailable){return new Result("PENDING",null);}}
}
