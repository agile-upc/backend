package com.agrotech.api.management.domain.services;

import com.agrotech.api.management.domain.model.aggregates.Crop;
import com.agrotech.api.management.domain.model.commands.CreateCropCommand;
import com.agrotech.api.management.domain.model.commands.DeleteCropCommand;
import com.agrotech.api.management.domain.model.commands.UpdateCropCommand;
import com.agrotech.api.management.domain.model.commands.UpdateIotCropCommand;

import java.util.Optional;

public interface CropCommandService {
    Long handle(CreateCropCommand command);
    Optional<Crop> handle(UpdateCropCommand command);
    void handle(DeleteCropCommand command);
    Optional<Crop> handle(UpdateIotCropCommand command);
}
