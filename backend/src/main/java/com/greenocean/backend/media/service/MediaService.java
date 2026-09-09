package com.greenocean.backend.media.service;

import com.greenocean.backend.common.exception.ForbiddenException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.media.dto.MediaUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaService {
    private static final Set<String> POST_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> VERIFICATION_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");
    private final JdbcTemplate jdbc;
    private final DatabaseUuidGenerator ids;
    private final MediaStorage storage;
    private final long maxBytes;
    private final Duration pendingTtl;

    public MediaService(JdbcTemplate jdbc, DatabaseUuidGenerator ids, MediaStorage storage,
                        @Value("${greenocean.media.max-bytes}") long maxBytes,
                        @Value("${greenocean.media.pending-ttl}") Duration pendingTtl) {
        this.jdbc = jdbc;
        this.ids = ids;
        this.storage = storage;
        this.maxBytes = maxBytes;
        this.pendingTtl = pendingTtl;
    }

    @Transactional
    public MediaUploadResponse upload(UUID userId, String purpose, MultipartFile file) {
        String normalizedPurpose = purpose == null ? "POST" : purpose.toUpperCase(Locale.ROOT);
        validate(normalizedPurpose, file);
        UUID id = ids.nextUuid();
        String extension = extension(file.getContentType());
        String key = userId + "/" + id + extension;
        Instant expiresAt = Instant.now().plus(pendingTtl);
        try {
            storage.store(key, file);
            jdbc.update("""
                    INSERT INTO media (id, uploader_id, storage_key, original_filename, content_type, size_bytes,
                                       media_status, purpose, expires_at)
                    VALUES (?, ?, ?, ?, ?, ?, 'PENDING', ?, ?)
                    """, id, userId, key, cleanFilename(file.getOriginalFilename()), file.getContentType(),
                    file.getSize(), normalizedPurpose, expiresAt);
            return new MediaUploadResponse(id, cleanFilename(file.getOriginalFilename()), file.getContentType(),
                    file.getSize(), normalizedPurpose, "PENDING", expiresAt);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store media", exception);
        } catch (RuntimeException exception) {
            try { storage.delete(key); } catch (IOException ignored) { }
            throw exception;
        }
    }

    @Transactional
    public void attachToPost(UUID userId, UUID postId, List<UUID> mediaIds) {
        if (mediaIds == null || mediaIds.isEmpty()) return;
        if (mediaIds.stream().distinct().count() != mediaIds.size()) {
            throw new IllegalArgumentException("mediaIds must be unique");
        }
        int updated = mediaIds.stream().mapToInt(mediaId -> jdbc.update("""
                UPDATE media SET post_id = ?, media_status = 'ATTACHED', expires_at = NULL
                 WHERE id = ? AND uploader_id = ? AND purpose = 'POST' AND media_status = 'PENDING'
                   AND expires_at > CURRENT_TIMESTAMP
                """, postId, mediaId, userId)).sum();
        if (updated != mediaIds.size()) throw new ForbiddenException("One or more media uploads are unavailable");
    }

    @Transactional(readOnly = true)
    public Download download(UUID mediaId, UUID userId) {
        return jdbc.query("""
                SELECT m.storage_key, m.original_filename, m.content_type FROM media m
                 WHERE m.id = ? AND m.deleted_at IS NULL
                   AND (m.uploader_id = ? OR (m.purpose = 'POST' AND m.media_status = 'ATTACHED' AND EXISTS (
                       SELECT 1 FROM posts p WHERE p.id = m.post_id AND p.status = 'PUBLISHED' AND (
                         p.author_id = ? OR p.visibility = 'PUBLIC'
                         OR (p.visibility = 'FOLLOWERS' AND EXISTS (SELECT 1 FROM follows f WHERE f.follower_id = ? AND f.following_id = p.author_id))
                         OR (p.visibility = 'COMMUNITY' AND EXISTS (SELECT 1 FROM community_members cm WHERE cm.community_id = p.community_id AND cm.user_id = ?))
                       ) AND NOT EXISTS (SELECT 1 FROM blocks b WHERE
                         (b.blocker_id = ? AND b.blocked_id = p.author_id) OR (b.blocked_id = ? AND b.blocker_id = p.author_id))
                   )))
                """, (rs, row) -> new Download(storage.load(rs.getString("storage_key")),
                rs.getString("original_filename"), rs.getString("content_type")), mediaId, userId, userId, userId, userId, userId, userId)
                .stream().findFirst().orElseThrow(() -> new NotFoundException("Media was not found"));
    }

    @Transactional
    public void deletePending(UUID mediaId, UUID userId) {
        List<String> keys = jdbc.query("""
                UPDATE media SET media_status = 'DELETED', deleted_at = CURRENT_TIMESTAMP
                 WHERE id = ? AND uploader_id = ? AND media_status = 'PENDING'
                 RETURNING storage_key
                """, (rs, row) -> rs.getString(1), mediaId, userId);
        if (keys.isEmpty()) throw new NotFoundException("Pending media was not found");
        try { storage.delete(keys.getFirst()); } catch (IOException ignored) { }
    }

    @Scheduled(fixedDelayString = "PT1H")
    @Transactional
    public void purgeExpiredUploads() {
        List<String> keys = jdbc.query("""
                DELETE FROM media WHERE media_status = 'PENDING' AND expires_at < CURRENT_TIMESTAMP RETURNING storage_key
                """, (rs, row) -> rs.getString(1));
        keys.forEach(key -> { try { storage.delete(key); } catch (IOException ignored) { } });
    }

    private void validate(String purpose, MultipartFile file) {
        if (!Set.of("POST", "VERIFICATION", "PROFILE").contains(purpose)) throw new IllegalArgumentException("Invalid media purpose");
        if (file.isEmpty() || file.getSize() > maxBytes) throw new IllegalArgumentException("File is empty or exceeds the upload limit");
        String type = file.getContentType();
        Set<String> allowed = "VERIFICATION".equals(purpose) ? VERIFICATION_TYPES : POST_TYPES;
        if (type == null || !allowed.contains(type.toLowerCase(Locale.ROOT))) throw new IllegalArgumentException("Unsupported media type");
        try {
            if (!matchesSignature(type.toLowerCase(Locale.ROOT), file.getInputStream())) throw new IllegalArgumentException("File content does not match its media type");
        } catch (IOException exception) { throw new IllegalArgumentException("Could not inspect uploaded file", exception); }
    }

    private boolean matchesSignature(String type, InputStream stream) throws IOException {
        byte[] header = stream.readNBytes(12);
        return switch (type) {
            case "image/jpeg" -> header.length >= 3 && (header[0] & 0xff) == 0xff && (header[1] & 0xff) == 0xd8 && (header[2] & 0xff) == 0xff;
            case "image/png" -> header.length >= 8 && (header[0] & 0xff) == 0x89 && header[1] == 0x50 && header[2] == 0x4e && header[3] == 0x47;
            case "image/gif" -> header.length >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F';
            case "image/webp" -> header.length >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
            case "application/pdf" -> header.length >= 5 && header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F' && header[4] == '-';
            default -> false;
        };
    }

    private String cleanFilename(String name) {
        if (name == null) return null;
        String normalized = name.replace('\\', '/');
        String cleaned = normalized.substring(normalized.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "");
        return cleaned.length() > 255 ? cleaned.substring(cleaned.length() - 255) : cleaned;
    }

    private String extension(String type) {
        return switch (type == null ? "" : type) {
            case "image/jpeg" -> ".jpg"; case "image/png" -> ".png"; case "image/webp" -> ".webp";
            case "image/gif" -> ".gif"; case "application/pdf" -> ".pdf"; default -> ".bin";
        };
    }

    public record Download(Resource resource, String filename, String contentType) {}
}
