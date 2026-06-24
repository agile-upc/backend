package com.agrotech.api.education.application.usecase;

import com.agrotech.api.education.application.mapper.EducationalResourceMapper;
import com.agrotech.api.education.domain.model.EducationalResource;
import com.agrotech.api.education.infrastructure.persistence.jpa.repository.EducationalResourceRepository;
import com.agrotech.api.education.infrastructure.web.dto.ImportEducationalResourceResource;
import com.agrotech.api.education.infrastructure.web.dto.ImportEducationalResourcesRequest;
import com.agrotech.api.education.infrastructure.web.dto.ImportEducationalResourcesResponse;
import com.agrotech.api.iam.application.usecase.AuthenticatedUserService;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EducationalResourceService {
    private final EducationalResourceRepository educationalResourceRepository;
    private final EducationalResourceMapper educationalResourceMapper;
    private final AuthenticatedUserService authenticatedUserService;

    public EducationalResourceService(
            EducationalResourceRepository educationalResourceRepository,
            EducationalResourceMapper educationalResourceMapper,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.educationalResourceRepository = educationalResourceRepository;
        this.educationalResourceMapper = educationalResourceMapper;
        this.authenticatedUserService = authenticatedUserService;
    }

    public List<EducationalResource> getResources() {
        return educationalResourceRepository.findAllByOrderByPublishedAtDescUpdatedAtDesc();
    }

    @Transactional
    public ImportEducationalResourcesResponse importResources(ImportEducationalResourcesRequest request) {
        authenticatedUserService.requireRole(UserRole.ADMIN);
        if (request == null || request.resources() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "resources is required");
        }

        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (ImportEducationalResourceResource resource : request.resources()) {
            if (!isValid(resource)) {
                skipped++;
                continue;
            }

            var existing = educationalResourceRepository.findBySourceUrl(resource.sourceUrl().trim());
            if (existing.isPresent()) {
                educationalResourceMapper.updateEntity(existing.get(), normalize(resource));
                educationalResourceRepository.save(existing.get());
                updated++;
                continue;
            }

            educationalResourceRepository.save(educationalResourceMapper.toEntity(normalize(resource)));
            created++;
        }

        return new ImportEducationalResourcesResponse(created, updated, skipped);
    }

    private boolean isValid(ImportEducationalResourceResource resource) {
        return resource != null
                && hasText(resource.title())
                && resource.type() != null
                && hasText(resource.sourceName())
                && hasText(resource.sourceUrl())
                && resource.sourceUrl().trim().length() <= 768;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private ImportEducationalResourceResource normalize(ImportEducationalResourceResource resource) {
        return new ImportEducationalResourceResource(
                resource.title().trim(),
                trimToNull(resource.summary()),
                resource.type(),
                resource.sourceName().trim(),
                resource.sourceUrl().trim(),
                trimToNull(resource.downloadUrl()),
                trimToNull(resource.thumbnailUrl()),
                resource.publishedAt(),
                resource.topics()
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
