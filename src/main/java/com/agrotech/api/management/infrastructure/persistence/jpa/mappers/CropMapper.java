package com.agrotech.api.management.infrastructure.persistence.jpa.mappers;

import com.agrotech.api.management.domain.model.aggregates.Crop;
import com.agrotech.api.management.infrastructure.persistence.jpa.entities.CropEntity;
import com.agrotech.api.profile.infrastructure.persistence.jpa.mappers.FarmerMapper;

public class CropMapper {
    public static Crop toDomain(CropEntity entity) {
        if (entity == null) return null;
        return new Crop(
                entity.getId(),
                entity.getName(),
                entity.getTemperature(),
                entity.getHumidity(),
                entity.getTankMaxVolume(),
                entity.getTankHeight(),
                entity.getTankCurrentVolume(),
                entity.getTemperatureMaxThreshold(),
                entity.getHumidityMinThreshold(),
                FarmerMapper.toDomain(entity.getFarmer())
        );
    }

    public static CropEntity toEntity(Crop domain) {
        if (domain == null) return null;
        return CropEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .temperature(domain.getTemperature())
                .humidity(domain.getHumidity())
                .tankMaxVolume(domain.getTankMaxVolume())
                .tankHeight(domain.getTankHeight())
                .tankCurrentVolume(domain.getTankCurrentVolume())
                .temperatureMaxThreshold(domain.getTemperatureMaxThreshold())
                .humidityMinThreshold(domain.getHumidityMinThreshold())
                .farmer(FarmerMapper.toEntity(domain.getFarmer()))
                .build();
    }
}
