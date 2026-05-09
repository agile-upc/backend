package com.agrotech.api.management.interfaces.rest.transform;

import com.agrotech.api.management.domain.model.commands.CreateCropCommand;
import com.agrotech.api.management.interfaces.rest.resources.CreateCropResource;

public class CreateCropCommandFromResourceAssembler {
    public static CreateCropCommand toCommandFromResource(CreateCropResource resource) {
        return new CreateCropCommand(
                resource.name(),
                resource.tankMaxVolume(),
                resource.tankHeight(),
                resource.temperatureMaxThreshold(),
                resource.humidityMinThreshold(),
                resource.farmerId()
        );
    }
}
