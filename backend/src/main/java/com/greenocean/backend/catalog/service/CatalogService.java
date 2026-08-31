package com.greenocean.backend.catalog.service;

import com.greenocean.backend.catalog.dto.ArticleResponse;
import com.greenocean.backend.catalog.dto.CreateArticleRequest;
import com.greenocean.backend.catalog.dto.MediaRecommendationResponse;
import com.greenocean.backend.catalog.dto.ProfessionalResponse;
import com.greenocean.backend.catalog.repository.CatalogRepository;
import com.greenocean.backend.common.exception.ForbiddenException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.post.dto.SupportCategoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CatalogService {
    private final CatalogRepository repository;
    private final DatabaseUuidGenerator uuidGenerator;

    public CatalogService(CatalogRepository repository, DatabaseUuidGenerator uuidGenerator) {
        this.repository = repository;
        this.uuidGenerator = uuidGenerator;
    }

    @Transactional(readOnly = true)
    public List<SupportCategoryResponse> categories() { return repository.categories(); }
    @Transactional(readOnly = true)
    public List<ProfessionalResponse> professionals(String query, String specialty, boolean availableOnly) {
        return repository.professionals(query == null ? "" : query, specialty, availableOnly);
    }
    @Transactional(readOnly = true)
    public ProfessionalResponse professional(UUID id) {
        return repository.professional(id).orElseThrow(() -> new NotFoundException("Professional was not found"));
    }
    @Transactional(readOnly = true)
    public List<ArticleResponse> articles(UUID userId, String topic) { return repository.articles(userId, topic); }
    @Transactional(readOnly = true)
    public ArticleResponse article(UUID userId, UUID id) {
        return repository.article(userId, id).orElseThrow(() -> new NotFoundException("Article was not found"));
    }
    @Transactional
    public UUID createArticle(UUID userId, CreateArticleRequest request) {
        if (!repository.isVerifiedProfessional(userId)) {
            throw new ForbiddenException("Only verified professionals can submit articles");
        }
        UUID id = uuidGenerator.nextUuid();
        repository.createArticle(id, userId, request);
        return id;
    }
    @Transactional
    public void setArticleHelpful(UUID userId, UUID articleId, boolean helpful) {
        article(userId, articleId);
        if (helpful) repository.markArticleHelpful(articleId, userId); else repository.removeArticleHelpful(articleId, userId);
    }
    @Transactional(readOnly = true)
    public List<MediaRecommendationResponse> media(UUID userId, String kind) { return repository.media(userId, kind); }
    @Transactional
    public void setMediaSaved(UUID userId, UUID mediaId, boolean saved) { repository.setMediaSaved(mediaId, userId, saved); }
}
