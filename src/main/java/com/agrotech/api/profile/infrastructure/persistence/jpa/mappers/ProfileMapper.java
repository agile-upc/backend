package com.agrotech.api.profile.infrastructure.persistence.jpa.mappers;

import com.agrotech.api.iam.infrastructure.persistence.jpa.mappers.UserMapper;
import com.agrotech.api.profile.domain.model.aggregates.Profile;
import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.ProfileEntity;

public class ProfileMapper {
    public static Profile toDomain(ProfileEntity entity) {
        if (entity == null) return null;
        return new Profile(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getCity(),
                entity.getCountry(),
                entity.getBirthDate(),
                entity.getDescription(),
                entity.getPhoto(),
                entity.getOccupation(),
                entity.getExperience(),
                UserMapper.toDomain(entity.getUser())
        );
    }

    public static ProfileEntity toEntity(Profile domain) {
        if (domain == null) return null;
        return ProfileEntity.builder()
                .id(domain.getId())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .city(domain.getCity())
                .country(domain.getCountry())
                .birthDate(domain.getBirthDate())
                .description(domain.getDescription())
                .photo(domain.getPhoto())
                .occupation(domain.getOccupation())
                .experience(domain.getExperience())
                .user(UserMapper.toEntity(domain.getUser()))
                .build();
    }
}
