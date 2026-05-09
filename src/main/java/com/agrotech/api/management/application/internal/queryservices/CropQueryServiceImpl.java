package com.agrotech.api.management.application.internal.queryservices;

import com.agrotech.api.management.domain.model.aggregates.Crop;
import com.agrotech.api.management.domain.model.queries.GetAllCropsByFarmerIdQuery;
import com.agrotech.api.management.domain.model.queries.GetAllCropsQuery;
import com.agrotech.api.management.domain.model.queries.GetCropByIdQuery;
import com.agrotech.api.management.domain.services.CropQueryService;
import com.agrotech.api.management.infrastructure.persistence.jpa.mappers.CropMapper;
import com.agrotech.api.management.infrastructure.persistence.jpa.repositories.CropRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CropQueryServiceImpl implements CropQueryService {
    private final CropRepository cropRepository;

    public CropQueryServiceImpl(CropRepository cropRepository) {
        this.cropRepository = cropRepository;
    }

    @Override
    public List<Crop> handle(GetAllCropsByFarmerIdQuery query) {
        return this.cropRepository.findAllByFarmer_Id(query.farmerId())
                .stream()
                .map(CropMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Crop> handle(GetAllCropsQuery query) {
        return this.cropRepository.findAll()
                .stream()
                .map(CropMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Crop> handle(GetCropByIdQuery query) {
        return this.cropRepository.findById(query.cropId())
                .map(CropMapper::toDomain);
    }
}
