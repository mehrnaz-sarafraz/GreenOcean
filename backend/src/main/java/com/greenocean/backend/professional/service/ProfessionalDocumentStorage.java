package com.greenocean.backend.professional.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Component
public class ProfessionalDocumentStorage {
    private final Path root;

    public ProfessionalDocumentStorage(@Value("${greenocean.storage.professional-documents-root:${java.io.tmpdir}/greenocean-professional-documents}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    public StoredDocument store(UUID applicationId, MultipartFile file) {
        String contentType = file.getContentType() == null
                ? "application/octet-stream"
                : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.equals("application/pdf") && !contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
            throw new IllegalArgumentException("Only PDF, JPEG, and PNG credential documents are accepted");
        }
        if (file.isEmpty() || file.getSize() > 10 * 1024 * 1024L) {
            throw new IllegalArgumentException("Credential documents must be between 1 byte and 10 MB");
        }
        validateFileSignature(file, contentType);
        String originalName = safeName(file.getOriginalFilename());
        String extension = contentType.equals("application/pdf") ? ".pdf" : contentType.equals("image/png") ? ".png" : ".jpg";
        String storageKey = applicationId + "/" + UUID.randomUUID() + extension;
        Path target = root.resolve(storageKey).normalize();
        if (!target.startsWith(root)) throw new IllegalStateException("Invalid storage target");
        try {
            Files.createDirectories(target.getParent());
            Path temporary = Files.createTempFile(target.getParent(), "upload-", ".tmp");
            String sha256;
            try (InputStream input = file.getInputStream()) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                try (DigestInputStream hashed = new DigestInputStream(input, digest)) {
                    Files.copy(hashed, temporary, StandardCopyOption.REPLACE_EXISTING);
                }
                sha256 = HexFormat.of().formatHex(digest.digest());
            }
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredDocument(originalName, contentType, file.getSize(), sha256, storageKey);
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Credential document could not be stored", exception);
        }
    }

    private void validateFileSignature(MultipartFile file, String contentType) {
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(8);
            boolean valid = switch (contentType) {
                case "application/pdf" -> startsWith(header, new byte[]{'%', 'P', 'D', 'F', '-'});
                case "image/jpeg" -> startsWith(header, new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff});
                case "image/png" -> Arrays.equals(header,
                        new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'});
                default -> false;
            };
            if (!valid) {
                throw new IllegalArgumentException("Credential document contents do not match the declared file type");
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("Credential document could not be inspected", exception);
        }
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) return false;
        for (int index = 0; index < prefix.length; index++) {
            if (value[index] != prefix[index]) return false;
        }
        return true;
    }

    private String safeName(String name) {
        String value = name == null || name.isBlank() ? "credential" : Path.of(name).getFileName().toString().trim();
        return value.length() > 240 ? value.substring(value.length() - 240) : value;
    }

    public void delete(String storageKey) {
        Path target = root.resolve(storageKey).normalize();
        if (!target.startsWith(root)) throw new IllegalArgumentException("Invalid storage key");
        try { Files.deleteIfExists(target); }
        catch (IOException exception) { throw new IllegalStateException("Credential document could not be deleted", exception); }
    }

    public record StoredDocument(String originalName, String contentType, long byteSize, String sha256, String storageKey) {}
}
