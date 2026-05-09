package com.agrotech.api.iam.infrastructure.persistence.jpa.mappers;

import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.infrastructure.persistence.jpa.entities.RoleEntity;

public class RoleMapper {

    public static Role toDomain(RoleEntity entity) {
        if (entity == null) return null;
        return new Role(entity.getId(), entity.getName());
    }

    public static RoleEntity toEntity(Role domain) {
        if (domain == null) return null;
        return RoleEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .build();
    }
}