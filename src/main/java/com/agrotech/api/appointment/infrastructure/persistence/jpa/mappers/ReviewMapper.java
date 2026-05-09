package com.agrotech.api.appointment.infrastructure.persistence.jpa.mappers;

import com.agrotech.api.appointment.domain.model.entities.Review;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.entities.ReviewEntity;
import com.agrotech.api.profile.infrastructure.persistence.jpa.mappers.AdvisorMapper;
import com.agrotech.api.profile.infrastructure.persistence.jpa.mappers.FarmerMapper;

public class ReviewMapper {
    public static Review toDomain(ReviewEntity entity) {
        if (entity == null) return null;
        return new Review(
                entity.getId(),
                entity.getComment(),
                entity.getRating(),
                AdvisorMapper.toDomain(entity.getAdvisor()),
                FarmerMapper.toDomain(entity.getFarmer())
        );
    }

    public static ReviewEntity toEntity(Review domain) {
        if (domain == null) return null;
        return ReviewEntity.builder()
                .id(domain.getId())
                .comment(domain.getComment())
                .rating(domain.getRating())
                .advisor(AdvisorMapper.toEntity(domain.getAdvisor()))
                .farmer(FarmerMapper.toEntity(domain.getFarmer()))
                .build();
    }
}