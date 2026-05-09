package com.agrotech.api.management.domain.services;

import com.agrotech.api.management.domain.model.aggregates.Crop;
import com.agrotech.api.management.domain.model.queries.GetAllCropsByFarmerIdQuery;
import com.agrotech.api.management.domain.model.queries.GetAllCropsQuery;
import com.agrotech.api.management.domain.model.queries.GetCropByIdQuery;

import java.util.List;
import java.util.Optional;

public interface CropQueryService {
    List<Crop> handle(GetAllCropsByFarmerIdQuery query);
    List<Crop> handle(GetAllCropsQuery query);
    Optional<Crop> handle(GetCropByIdQuery query);
}
