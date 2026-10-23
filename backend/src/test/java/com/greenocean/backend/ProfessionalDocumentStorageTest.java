package com.greenocean.backend;

import com.greenocean.backend.professional.service.ProfessionalDocumentStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProfessionalDocumentStorageTest {
    @TempDir
    Path storageRoot;

    @Test
    void shouldAcceptARealPdfHeader() {
        ProfessionalDocumentStorage storage = new ProfessionalDocumentStorage(storageRoot.toString());
        MockMultipartFile document = new MockMultipartFile("file", "license.pdf", "application/pdf",
                "%PDF-1.4\nfixture".getBytes(StandardCharsets.US_ASCII));

        ProfessionalDocumentStorage.StoredDocument stored = storage.store(UUID.randomUUID(), document);

        assertEquals("application/pdf", stored.contentType());
        assertEquals(document.getSize(), stored.byteSize());
    }

    @Test
    void shouldRejectMimeTypeSpoofing() {
        ProfessionalDocumentStorage storage = new ProfessionalDocumentStorage(storageRoot.toString());
        MockMultipartFile document = new MockMultipartFile("file", "license.pdf", "application/pdf",
                "not a PDF".getBytes(StandardCharsets.US_ASCII));

        assertThrows(IllegalArgumentException.class, () -> storage.store(UUID.randomUUID(), document));
    }
}
