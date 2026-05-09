package com.agrotech.api.management.domain.services;

import com.agrotech.api.management.domain.model.aggregates.Enclosure;
import com.agrotech.api.management.domain.model.queries.GetAllEnclosuresByFarmerIdQuery;
import com.agrotech.api.management.domain.model.queries.GetAllEnclosuresQuery;
import com.agrotech.api.management.domain.model.queries.GetEnclosureByIdQuery;

import java.util.List;
import java.util.Optional;

public interface EnclosureQueryService {
    List<Enclosure> handle(GetAllEnclosuresQuery query);
    Optional<Enclosure> handle(GetEnclosureByIdQuery query);
    List<Enclosure> handle(GetAllEnclosuresByFarmerIdQuery query);
}
