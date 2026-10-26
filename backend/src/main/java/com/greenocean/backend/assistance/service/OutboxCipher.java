package com.greenocean.backend.assistance.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

@Component
public class OutboxCipher {
    private final String activeId;
    private final java.util.Map<String,String> secrets=new java.util.LinkedHashMap<>();
    public OutboxCipher(String secret){this(secret,"primary","");}
    @org.springframework.beans.factory.annotation.Autowired
    public OutboxCipher(@Value("${greenocean.mail.outbox-key:${greenocean.auth.jwt-secret}}") String secret,
                        @Value("${greenocean.mail.key-id:primary}") String id,
                        @Value("${greenocean.mail.previous-keys:}") String previous) {
        if(!id.matches("[A-Za-z0-9_-]{1,32}")||secret.isBlank())throw new IllegalStateException("Invalid delivery key configuration");
        activeId=id;secrets.put(id,secret);
        if(!previous.isBlank())for(String entry:previous.split(";")) {
            String[] pair=entry.split("=",2);
            if(pair.length!=2||!pair[0].matches("[A-Za-z0-9_-]{1,32}")||pair[1].isBlank()||secrets.putIfAbsent(pair[0],pair[1])!=null||secrets.size()>4)
                throw new IllegalStateException("Invalid delivery key ring");
        }
    }
    private byte[] derive(String purpose,String secret) {
        try{return MessageDigest.getInstance("SHA-256").digest((purpose+secret).getBytes(StandardCharsets.UTF_8));}
        catch(Exception e){throw new IllegalStateException("Cannot initialize outbox protection");}
    }
    public String encrypt(String value) {
        try {
            byte[] nonce=new byte[12];new SecureRandom().nextBytes(nonce);
            Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(derive("outbox-v2:aes:",secrets.get(activeId)),"AES"),new GCMParameterSpec(128,nonce));
            byte[] encrypted=c.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] result=new byte[nonce.length+encrypted.length];System.arraycopy(nonce,0,result,0,12);System.arraycopy(encrypted,0,result,12,encrypted.length);
            return "v2:"+activeId+":"+Base64.getEncoder().encodeToString(result);
        } catch(Exception e) {throw new IllegalStateException("Cannot protect delivery payload");}
    }
    public String decrypt(String value) {
        if(value.startsWith("v2:")) {
            String[] parts=value.split(":",3);
            if(parts.length!=3||!secrets.containsKey(parts[1]))throw new IllegalStateException("Delivery key is unavailable");
            return decryptWith(parts[2],derive("outbox-v2:aes:",secrets.get(parts[1])));
        }
        // Existing v1 queued ciphertext remains readable during the documented rotation overlap.
        for(String secret:secrets.values())try{return decryptWith(value,derive("outbox-v1:",secret));}catch(IllegalStateException ignored){}
        throw new IllegalStateException("Cannot read delivery payload");
    }
    private String decryptWith(String value,byte[] key) {
        try {
            byte[] input=Base64.getDecoder().decode(value);
            Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,input,0,12));
            return new String(c.doFinal(input,12,input.length-12),StandardCharsets.UTF_8);
        } catch(Exception e) {throw new IllegalStateException("Cannot read delivery payload");}
    }
    public String codeHash(String email,String code) {
        return "v2:"+activeId+":"+hash(email,code,derive("outbox-v2:verification:",secrets.get(activeId)));
    }
    public java.util.List<String> verificationHashes(String email,String code) {
        var hashes=new java.util.ArrayList<String>();
        for(var entry:secrets.entrySet()) {
            hashes.add("v2:"+entry.getKey()+":"+hash(email,code,derive("outbox-v2:verification:",entry.getValue())));
            hashes.add(hash(email,code,derive("outbox-v1:",entry.getValue())));
        }
        return java.util.List.copyOf(hashes);
    }
    private String hash(String email,String code,byte[] key) {
        try {
            var mac=javax.crypto.Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(key,"HmacSHA256"));
            return java.util.HexFormat.of().formatHex(mac.doFinal((email+":"+code).getBytes(StandardCharsets.UTF_8)));
        } catch(Exception e) {throw new IllegalStateException("Cannot protect verification code");}
    }
}
