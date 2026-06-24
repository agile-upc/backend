package com.agrotech.api.education.infrastructure.persistence.jpa.repository;

import com.agrotech.api.education.domain.model.EducationalResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EducationalResourceRepository extends JpaRepository<EducationalResource, Long> {
    Optional<EducationalResource> findBySourceUrl(String sourceUrl);
    List<EducationalResource> findAllByOrderByPublishedAtDescUpdatedAtDesc();
}
