package com.greenocean.backend;
import com.greenocean.backend.professional.service.PrivateDocumentCipher;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class PrivateDocumentCipherTest {
 private static final String OLD="old:"+java.util.Base64.getEncoder().encodeToString(new byte[32]);
 @Test void ciphertextIsOwnerBoundAndAuthenticated(){var c=new PrivateDocumentCipher(OLD,"old");byte[] data="Synthetic private credential".getBytes();byte[] encrypted=c.seal(data,"credential:a");assertArrayEquals(data,c.open(encrypted,"credential:a"));assertThrows(IllegalStateException.class,()->c.open(encrypted,"credential:b"));encrypted[encrypted.length-1]^=1;assertThrows(IllegalStateException.class,()->c.open(encrypted,"credential:a"));}
 @Test void rotationReadsOldEnvelopeWithoutUsingOldKeyForNewWrites(){byte[] next=new byte[32];java.util.Arrays.fill(next,(byte)7);String ring=OLD+",new:"+java.util.Base64.getEncoder().encodeToString(next);var old=new PrivateDocumentCipher(OLD,"old");var rotated=new PrivateDocumentCipher(ring,"new");byte[] data="Synthetic private credential".getBytes();assertArrayEquals(data,rotated.open(old.seal(data,"credential:a"),"credential:a"));byte[] fresh=rotated.seal(data,"credential:a");assertThrows(IllegalStateException.class,()->old.open(fresh,"credential:a"));}
 @Test void missingCurrentKeyFailsClosed(){assertThrows(RuntimeException.class,()->new PrivateDocumentCipher("","missing").seal(new byte[1],"credential:a"));}
}
