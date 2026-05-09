package com.agrotech.api.post.infrastructure.persistence.jpa.mappers;

import com.agrotech.api.post.domain.model.aggregates.Post;
import com.agrotech.api.post.infrastructure.persistence.jpa.entities.PostEntity;
import com.agrotech.api.profile.infrastructure.persistence.jpa.mappers.AdvisorMapper;

public class PostMapper {

    public static Post toDomain(PostEntity entity) {
        if (entity == null) return null;
        return new Post(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getImage(),
                AdvisorMapper.toDomain(entity.getAdvisor())
        );
    }

    public static PostEntity toEntity(Post domain) {
        if (domain == null) return null;
        return PostEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .image(domain.getImage())
                .advisor(AdvisorMapper.toEntity(domain.getAdvisor()))
                .build();
    }
}