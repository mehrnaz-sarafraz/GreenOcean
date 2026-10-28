package com.greenocean.backend.catalog.service;

import com.greenocean.backend.catalog.dto.ArticleResponse;
import com.greenocean.backend.catalog.dto.CatalogOptionResponse;
import com.greenocean.backend.catalog.dto.CrisisResourceResponse;
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
    public List<CatalogOptionResponse> options(String group) { return repository.options(group); }
    @Transactional(readOnly = true)
    public List<CrisisResourceResponse> crisisResources() { return repository.crisisResources(); }
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
        throw new com.greenocean.backend.common.exception.ConflictException("Use the versioned Growth Library editorial workflow; publication and featuring need separate current permissions.");
    }
    @Transactional
    public void setArticleHelpful(UUID userId, UUID articleId, boolean helpful) {
        article(userId, articleId);
        if (helpful) repository.markArticleHelpful(articleId, userId); else repository.removeArticleHelpful(articleId, userId);
    }
    @Transactional
    public void setArticleSaved(UUID userId, UUID articleId, boolean saved) {
        article(userId, articleId);
        repository.setArticleSaved(articleId, userId, saved);
    }
    @Transactional(readOnly = true)
    public List<MediaRecommendationResponse> media(UUID userId, String kind) { return repository.media(userId, kind); }
    @Transactional
    public void setMediaSaved(UUID userId, UUID mediaId, boolean saved) { if(saved && repository.media(userId,"All").stream().noneMatch(item->item.id().equals(mediaId)))throw new NotFoundException("Reviewed media is unavailable"); repository.setMediaSaved(mediaId, userId, saved); }
}
