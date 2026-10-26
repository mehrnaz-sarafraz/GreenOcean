package com.greenocean.backend;
import com.greenocean.backend.assistance.service.*;
import org.junit.jupiter.api.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class PhaseDProtectionTest {
 @Test @Tag("happy") void encryptionAndHmacUseVersionedSeparatedKeys(){
   var cipher=new OutboxCipher("synthetic-D-key-only","test","");String encrypted=cipher.encrypt("000123");
   assertThat(encrypted).startsWith("v2:test:").doesNotContain("000123");assertThat(cipher.decrypt(encrypted)).isEqualTo("000123");
   assertThat(cipher.verificationHashes("a@example.test","000123")).contains(cipher.codeHash("a@example.test","000123"));
   assertThat(cipher.codeHash("a@example.test","000123")).isNotEqualTo(cipher.codeHash("b@example.test","000123"));
 }
 @Test @Tag("creative") void pendingPayloadAndCodeRemainUsableDuringKeyRotation(){
   var old=new OutboxCipher("synthetic-old-key","old","");var next=new OutboxCipher("synthetic-new-key","new","old=synthetic-old-key");
   assertThat(next.decrypt(old.encrypt("000007"))).isEqualTo("000007");
   assertThat(next.verificationHashes("a@example.test","000007")).contains(old.codeHash("a@example.test","000007"));
   assertThat(next.encrypt("000008")).startsWith("v2:new:");
 }
 @Test @Tag("creative") void v1CiphertextAndHmacRemainCompatibleWithoutRewritingQueuedEntries()throws Exception{
   String secret="synthetic-legacy-key",value="000007",email="legacy@example.test";
   byte[] key=MessageDigest.getInstance("SHA-256").digest(("outbox-v1:"+secret).getBytes(StandardCharsets.UTF_8));byte[] nonce=new byte[12];
   var aes=javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");aes.init(javax.crypto.Cipher.ENCRYPT_MODE,new javax.crypto.spec.SecretKeySpec(key,"AES"),new javax.crypto.spec.GCMParameterSpec(128,nonce));
   byte[] data=aes.doFinal(value.getBytes(StandardCharsets.UTF_8)),payload=new byte[12+data.length];System.arraycopy(data,0,payload,12,data.length);
   var rotated=new OutboxCipher("synthetic-next-key","next","legacy="+secret);
   assertThat(rotated.decrypt(Base64.getEncoder().encodeToString(payload))).isEqualTo(value);
   var mac=javax.crypto.Mac.getInstance("HmacSHA256");mac.init(new javax.crypto.spec.SecretKeySpec(key,"HmacSHA256"));
   assertThat(rotated.verificationHashes(email,value)).contains(HexFormat.of().formatHex(mac.doFinal((email+":"+value).getBytes(StandardCharsets.UTF_8))));
 }
 @Test @Tag("malicious") void unknownKeysCorruptionAndInvalidKeyRingsFailWithoutLeakingValues(){
   var a=new OutboxCipher("synthetic-a","a","");var b=new OutboxCipher("synthetic-b","b","");
   assertThatThrownBy(()->b.decrypt(a.encrypt("000007"))).isInstanceOf(IllegalStateException.class).hasMessageNotContaining("000007");
   assertThatThrownBy(()->a.decrypt("v2:a:corrupted")).isInstanceOf(IllegalStateException.class);
   assertThatThrownBy(()->new OutboxCipher("synthetic-a","a","a=duplicate")).isInstanceOf(IllegalStateException.class);
 }
 @Test @Tag("confused") void verificationLimitConfigurationRejectsUnsafeValues(){
   assertThatThrownBy(()->new VerificationPolicy(0,5,3,java.time.Duration.ofMinutes(15),60)).isInstanceOf(IllegalArgumentException.class);
   assertThatThrownBy(()->new VerificationPolicy(5,5,3,java.time.Duration.ZERO,60)).isInstanceOf(IllegalArgumentException.class);
 }
}
