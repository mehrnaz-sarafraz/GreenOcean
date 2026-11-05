package com.greenocean.backend;
import com.greenocean.backend.professional.service.ProfessionalDocumentStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.file.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class PhaseQ4ObjectRecoveryTest {
    @TempDir Path temporary;
    ProfessionalDocumentStorage storage(){return new ProfessionalDocumentStorage(temporary.resolve("objects").toString(),"qa:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","qa");}
    MockMultipartFile document(){return new MockMultipartFile("file","synthetic.pdf","application/pdf","%PDF-1.4\nSYNTHETIC-Q4-PRIVATE-CREDENTIAL".getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    Path object(ProfessionalDocumentStorage.StoredDocument document){
        Path root=temporary.resolve("objects").toAbsolutePath().normalize();Path target=root.resolve(document.storageKey()).normalize();
        assertThat(target.startsWith(root)).isTrue();return target;
    }
    @Test void missingEncryptedObjectIsNotReplacedWithFabricatedContent()throws Exception{
        var storage=storage();UUID application=UUID.randomUUID();var metadata=storage.store(application,document());
        byte[] backup=Files.readAllBytes(object(metadata));Files.delete(object(metadata));
        assertThatThrownBy(()->storage.read(metadata.storageKey(),application)).isInstanceOf(IllegalArgumentException.class);
        Files.write(object(metadata),backup);
        assertThat(storage.read(metadata.storageKey(),application)).isEqualTo(document().getBytes());
    }
    @Test void corruptRestoredCiphertextAndWrongApplicationBindingFailClosed()throws Exception{
        var storage=storage();UUID application=UUID.randomUUID();var metadata=storage.store(application,document());
        byte[] backup=Files.readAllBytes(object(metadata)),corrupt=backup.clone();corrupt[corrupt.length-1]^=1;
        Files.write(object(metadata),corrupt);
        assertThatThrownBy(()->storage.read(metadata.storageKey(),application)).isInstanceOf(IllegalStateException.class);
        Files.write(object(metadata),backup);
        assertThatThrownBy(()->storage.read(metadata.storageKey(),UUID.randomUUID())).isInstanceOf(IllegalStateException.class);
        assertThat(storage.read(metadata.storageKey(),application)).isEqualTo(document().getBytes());
    }
    @Test void unavailableHistoricalKeyDoesNotDowngradeToPlaintext()throws Exception{
        var storage=storage();UUID application=UUID.randomUUID();var metadata=storage.store(application,document());
        var unavailable=new ProfessionalDocumentStorage(temporary.resolve("objects").toString(),"","");
        assertThatThrownBy(()->unavailable.read(metadata.storageKey(),application)).isInstanceOf(IllegalStateException.class);
        assertThat(Files.readAllBytes(object(metadata))).isNotEqualTo(document().getBytes());
        assertThat(storage.read(metadata.storageKey(),application)).isEqualTo(document().getBytes());
    }
}
