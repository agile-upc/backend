package com.agrotech.api.management.application.internal.commandservices;

import com.agrotech.api.management.domain.exceptions.CropNotFoundException;
import com.agrotech.api.management.domain.model.aggregates.Crop;
import com.agrotech.api.management.domain.model.commands.CreateCropCommand;
import com.agrotech.api.management.domain.model.commands.DeleteCropCommand;
import com.agrotech.api.management.domain.model.commands.UpdateCropCommand;
import com.agrotech.api.management.domain.model.commands.UpdateIotCropCommand;
import com.agrotech.api.management.domain.services.CropCommandService;
import com.agrotech.api.management.infrastructure.persistence.jpa.mappers.CropMapper;
import com.agrotech.api.management.infrastructure.persistence.jpa.repositories.CropRepository;
import com.agrotech.api.profile.infrastructure.persistence.jpa.mappers.FarmerMapper;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repositories.FarmerRepository;
import com.agrotech.api.shared.domain.exceptions.FarmerNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CropCommandServiceImpl implements CropCommandService {
    private final CropRepository cropRepository;
    private final FarmerRepository farmerRepository;

    public CropCommandServiceImpl(CropRepository cropRepository, FarmerRepository farmerRepository) {
        this.cropRepository = cropRepository;
        this.farmerRepository = farmerRepository;
    }

    @Override
    public Long handle(CreateCropCommand command) {
        var farmer = farmerRepository.findById(command.farmerId())
                .orElseThrow(() -> new FarmerNotFoundException(command.farmerId()));
        var crop = new Crop(command, FarmerMapper.toDomain(farmer));
        var cropEntity = cropRepository.save(CropMapper.toEntity(crop));
        return cropEntity.getId();
    }

    @Override
    public Optional<Crop> handle(UpdateCropCommand command) {
        var cropEntity = cropRepository.findById(command.cropId())
                .orElseThrow(() -> new CropNotFoundException(command.cropId()));
        cropEntity.update(command);
        cropRepository.save(cropEntity);
        return Optional.of(CropMapper.toDomain(cropEntity));
    }

    @Override
    public void handle(DeleteCropCommand command) {
        var crop = cropRepository.findById(command.cropId())
                .orElseThrow(() -> new CropNotFoundException(command.cropId()));
        cropRepository.delete(crop);
    }

    @Override
    public Optional<Crop> handle(UpdateIotCropCommand command) {
        var crop = cropRepository.findById(command.cropId())
                .orElseThrow(() -> new CropNotFoundException(command.cropId()));
        crop.updateIotData(command);
        cropRepository.save(crop);
        return Optional.of(CropMapper.toDomain(crop));
    }
}
