package com.greenocean.backend.media.controller;

import com.greenocean.backend.media.dto.MediaUploadResponse;
import com.greenocean.backend.media.service.MediaService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {
    private final MediaService service;
    public MediaController(MediaService service) { this.service = service; }

    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> upload(@AuthenticationPrincipal Jwt jwt,
                                                       @RequestParam(defaultValue = "POST") String purpose,
                                                       @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(201).body(service.upload(userId(jwt), purpose, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> download(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        MediaService.Download download = service.download(id, userId(jwt));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(download.filename() == null ? id.toString() : download.filename(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=300")
                .body(download.resource());
    }

    @DeleteMapping("/uploads/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.deletePending(id, userId(jwt));
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
