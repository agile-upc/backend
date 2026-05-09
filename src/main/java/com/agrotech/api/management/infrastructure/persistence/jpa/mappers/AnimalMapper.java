package com.agrotech.api.management.infrastructure.persistence.jpa.mappers;

import com.agrotech.api.management.domain.model.entities.Animal;
import com.agrotech.api.management.infrastructure.persistence.jpa.entities.AnimalEntity;

public class AnimalMapper {
    public static Animal toDomain(AnimalEntity entity) {
        if (entity == null) return null;
        return new Animal(
                entity.getId(),
                entity.getName(),
                entity.getAge(),
                entity.getSpecies(),
                entity.getBreed(),
                entity.getGender(),
                entity.getWeight(),
                entity.getHealth(),
                EnclosureMapper.toDomain(entity.getEnclosure())
        );
    }

    public static AnimalEntity toEntity(Animal domain) {
        if (domain == null) return null;
        return AnimalEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .age(domain.getAge())
                .species(domain.getSpecies())
                .breed(domain.getBreed())
                .gender(domain.getGender())
                .weight(domain.getWeight())
                .health(domain.getHealth())
                .enclosure(EnclosureMapper.toEntity(domain.getEnclosure()))
                .build();
    }
}
