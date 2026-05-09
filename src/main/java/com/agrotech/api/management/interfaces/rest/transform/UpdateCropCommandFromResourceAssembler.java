package com.agrotech.api.management.interfaces.rest.transform;

import com.agrotech.api.management.domain.model.commands.UpdateCropCommand;
import com.agrotech.api.management.domain.model.commands.UpdateIotCropCommand;
import com.agrotech.api.management.interfaces.rest.resources.UpdateCropResource;
import com.agrotech.api.management.interfaces.rest.resources.UpdateIotCropResource;

public class UpdateCropCommandFromResourceAssembler {
    public static UpdateCropCommand toCommandFromResource(Long id, UpdateCropResource resource){
        return new UpdateCropCommand(
                id,
                resource.name(),
                resource.tankMaxVolume(),
                resource.tankHeight(),
                resource.temperatureMaxThreshold(),
                resource.humidityMinThreshold()
        );
    }

    public static UpdateIotCropCommand toIotCommandFromResource(Long id, UpdateIotCropResource resource) {
        return new UpdateIotCropCommand(
                id,
                resource.temperature(),
                resource.humidity(),
                resource.tankCurrentVolume()
        );
    }
}
