package com.agrotech.api.profile.infrastructure.persistence.jpa.mappers;

import com.agrotech.api.iam.infrastructure.persistence.jpa.mappers.UserMapper;
import com.agrotech.api.profile.domain.model.entities.Advisor;
import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.AdvisorEntity;

public class AdvisorMapper {
    public static Advisor toDomain(AdvisorEntity entity) {
        if (entity == null) return null;
        var advisor = new Advisor(UserMapper.toDomain(entity.getUser()), entity.getRating());
        advisor.setId(entity.getId());
        return advisor;
    }

    public static AdvisorEntity toEntity(Advisor domain) {
        if (domain == null) return null;
        return AdvisorEntity.builder()
                .id(domain.getId())
                .user(UserMapper.toEntity(domain.getUser()))
                .rating(domain.getRating())
                .build();
    }
}