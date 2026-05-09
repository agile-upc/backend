package com.agrotech.api.profile.infrastructure.persistence.jpa.mappers;

import com.agrotech.api.iam.infrastructure.persistence.jpa.mappers.UserMapper;
import com.agrotech.api.profile.domain.model.entities.Farmer;
import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.FarmerEntity;

public class FarmerMapper {
    public static Farmer toDomain(FarmerEntity entity) {
        if (entity == null) return null;
        var farmer = new Farmer(UserMapper.toDomain(entity.getUser()));
        farmer.setId(entity.getId());
        return farmer;
    }

    public static FarmerEntity toEntity(Farmer domain) {
        if (domain == null) return null;
        return FarmerEntity.builder()
                .id(domain.getId())
                .user(UserMapper.toEntity(domain.getUser()))
                .build();
    }
}