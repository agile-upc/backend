package com.agrotech.api.education.infrastructure.web.dto;

import com.agrotech.api.education.domain.valueobject.EducationalResourceType;

import java.time.LocalDate;
import java.util.List;

public record ImportEducationalResourceResource(
        String title,
        String summary,
        EducationalResourceType type,
        String sourceName,
        String sourceUrl,
        String downloadUrl,
        String thumbnailUrl,
        LocalDate publishedAt,
        List<String> topics
) {
}
