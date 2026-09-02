package com.greenocean.backend.catalog.controller;

import com.greenocean.backend.catalog.dto.ArticleResponse;
import com.greenocean.backend.catalog.dto.CreateArticleRequest;
import com.greenocean.backend.catalog.dto.MediaRecommendationResponse;
import com.greenocean.backend.catalog.dto.ProfessionalResponse;
import com.greenocean.backend.catalog.service.CatalogService;
import com.greenocean.backend.post.dto.SupportCategoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {
    private final CatalogService service;
    public CatalogController(CatalogService service) { this.service = service; }

    @GetMapping("/catalog/categories")
    public List<SupportCategoryResponse> categories() { return service.categories(); }

    @GetMapping("/professionals")
    public List<ProfessionalResponse> professionals(@AuthenticationPrincipal Jwt jwt,
                                                     @RequestParam(defaultValue = "") String q,
                                                     @RequestParam(defaultValue = "All") String specialty,
                                                     @RequestParam(defaultValue = "false") boolean availableOnly) {
        return service.professionals(userId(jwt), q, specialty, availableOnly);
    }

    @GetMapping("/professionals/{id}")
    public ProfessionalResponse professional(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.professional(userId(jwt), id);
    }

    @GetMapping("/articles")
    public List<ArticleResponse> articles(@AuthenticationPrincipal Jwt jwt,
                                          @RequestParam(defaultValue = "All") String topic) {
        return service.articles(userId(jwt), topic);
    }

    @GetMapping("/articles/{id}")
    public ArticleResponse article(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.article(userId(jwt), id);
    }

    @PostMapping("/articles")
    public ResponseEntity<Map<String, UUID>> createArticle(@AuthenticationPrincipal Jwt jwt,
                                                            @Valid @RequestBody CreateArticleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", service.createArticle(userId(jwt), request)));
    }

    @PutMapping("/articles/{id}/helpful")
    public ResponseEntity<Void> helpful(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.setArticleHelpful(userId(jwt), id, true);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/articles/{id}/helpful")
    public ResponseEntity<Void> notHelpful(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.setArticleHelpful(userId(jwt), id, false);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/articles/{id}/save")
    public ResponseEntity<Void> saveArticle(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.setArticleSaved(userId(jwt), id, true);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/articles/{id}/save")
    public ResponseEntity<Void> unsaveArticle(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.setArticleSaved(userId(jwt), id, false);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/media-recommendations")
    public List<MediaRecommendationResponse> media(@AuthenticationPrincipal Jwt jwt,
                                                    @RequestParam(defaultValue = "All") String kind) {
        return service.media(userId(jwt), kind);
    }

    @PutMapping("/media-recommendations/{id}/save")
    public ResponseEntity<Void> saveMedia(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.setMediaSaved(userId(jwt), id, true);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/media-recommendations/{id}/save")
    public ResponseEntity<Void> unsaveMedia(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.setMediaSaved(userId(jwt), id, false);
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
