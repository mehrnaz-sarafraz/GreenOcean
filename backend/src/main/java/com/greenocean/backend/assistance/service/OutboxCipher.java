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
    private final byte[] key;
    public OutboxCipher(@Value("${greenocean.mail.outbox-key:${greenocean.auth.jwt-secret}}") String secret) {
        try {key=MessageDigest.getInstance("SHA-256").digest(("outbox-v1:"+secret).getBytes(StandardCharsets.UTF_8));}
        catch(Exception e) {throw new IllegalStateException("Cannot initialize outbox protection");}
    }
    public String encrypt(String value) {
        try {
            byte[] nonce=new byte[12];new SecureRandom().nextBytes(nonce);
            Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,nonce));
            byte[] encrypted=c.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] result=new byte[nonce.length+encrypted.length];System.arraycopy(nonce,0,result,0,12);System.arraycopy(encrypted,0,result,12,encrypted.length);
            return Base64.getEncoder().encodeToString(result);
        } catch(Exception e) {throw new IllegalStateException("Cannot protect delivery payload");}
    }
    public String decrypt(String value) {
        try {
            byte[] input=Base64.getDecoder().decode(value);
            Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,input,0,12));
            return new String(c.doFinal(input,12,input.length-12),StandardCharsets.UTF_8);
        } catch(Exception e) {throw new IllegalStateException("Cannot read delivery payload");}
    }
    public String codeHash(String email,String code) {
        try {
            var mac=javax.crypto.Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(key,"HmacSHA256"));
            return java.util.HexFormat.of().formatHex(mac.doFinal((email+":"+code).getBytes(StandardCharsets.UTF_8)));
        } catch(Exception e) {throw new IllegalStateException("Cannot protect verification code");}
    }
}
