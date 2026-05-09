package com.agrotech.api.management.interfaces.rest.transform;

import com.agrotech.api.management.domain.model.aggregates.Crop;
import com.agrotech.api.management.interfaces.rest.resources.CropResource;

public class CropResourceFromEntityAssembler {
    public static CropResource toResourceFromEntity(Crop entity) {
        return new CropResource(
                entity.getId(),
                entity.getName(),
                entity.getTemperature(),
                entity.getHumidity(),
                entity.getTankMaxVolume(),
                entity.getTankHeight(),
                entity.getTankCurrentVolume(),
                entity.getTemperatureMaxThreshold(),
                entity.getHumidityMinThreshold()
        );
    }
}
