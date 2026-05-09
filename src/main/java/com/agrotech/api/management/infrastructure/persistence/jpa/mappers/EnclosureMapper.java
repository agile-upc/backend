package com.agrotech.api.management.infrastructure.persistence.jpa.mappers;

import com.agrotech.api.management.domain.model.aggregates.Enclosure;
import com.agrotech.api.management.infrastructure.persistence.jpa.entities.EnclosureEntity;
import com.agrotech.api.profile.infrastructure.persistence.jpa.mappers.FarmerMapper;

public class EnclosureMapper {
    public static Enclosure toDomain(EnclosureEntity entity) {
        if (entity == null) return null;
        return new Enclosure(
                entity.getId(),
                entity.getName(),
                entity.getCapacity(),
                entity.getType(),
                FarmerMapper.toDomain(entity.getFarmer())
        );
    }

    public static EnclosureEntity toEntity(Enclosure domain) {
        if (domain == null) return null;
        return EnclosureEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .capacity(domain.getCapacity())
                .type(domain.getType())
                .farmer(FarmerMapper.toEntity(domain.getFarmer()))
                .build();
    }
}
