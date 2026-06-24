package com.agrotech.api.education.application.mapper;

import com.agrotech.api.education.domain.model.EducationalResource;
import com.agrotech.api.education.infrastructure.web.dto.EducationalResourceResource;
import com.agrotech.api.education.infrastructure.web.dto.ImportEducationalResourceResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EducationalResourceMapper {
    public EducationalResourceResource toResource(EducationalResource resource) {
        return new EducationalResourceResource(
                resource.getId(),
                resource.getTitle(),
                resource.getSummary(),
                resource.getType(),
                resource.getSourceName(),
                resource.getSourceUrl(),
                resource.getDownloadUrl(),
                resource.getThumbnailUrl(),
                resource.getPublishedAt(),
                List.copyOf(resource.getTopics())
        );
    }

    public EducationalResource toEntity(ImportEducationalResourceResource resource) {
        return EducationalResource.builder()
                .title(resource.title())
                .summary(resource.summary())
                .type(resource.type())
                .sourceName(resource.sourceName())
                .sourceUrl(resource.sourceUrl())
                .downloadUrl(resource.downloadUrl())
                .thumbnailUrl(resource.thumbnailUrl())
                .publishedAt(resource.publishedAt())
                .topics(normalizeTopics(resource.topics()))
                .build();
    }

    public void updateEntity(EducationalResource entity, ImportEducationalResourceResource resource) {
        entity.setTitle(resource.title());
        entity.setSummary(resource.summary());
        entity.setType(resource.type());
        entity.setSourceName(resource.sourceName());
        entity.setDownloadUrl(resource.downloadUrl());
        entity.setThumbnailUrl(resource.thumbnailUrl());
        entity.setPublishedAt(resource.publishedAt());
        entity.setTopics(normalizeTopics(resource.topics()));
    }

    private List<String> normalizeTopics(List<String> topics) {
        if (topics == null) {
            return new ArrayList<>();
        }

        return topics.stream()
                .filter(topic -> topic != null && !topic.isBlank())
                .map(String::trim)
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
}
