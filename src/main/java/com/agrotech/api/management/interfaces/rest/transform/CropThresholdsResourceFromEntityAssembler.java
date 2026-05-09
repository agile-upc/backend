package com.agrotech.api.management.interfaces.rest.transform;

import com.agrotech.api.management.domain.model.aggregates.Crop;
import com.agrotech.api.management.interfaces.rest.resources.CropThresholdsResource;

public class CropThresholdsResourceFromEntityAssembler {
    public static CropThresholdsResource toResourceFromEntity(Crop entity) {
        return new CropThresholdsResource(
                entity.getTemperatureMaxThreshold(),
                entity.getHumidityMinThreshold(),
                entity.getTankMaxVolume(),
                entity.getTankHeight()
        );
    }
}
