package com.agrotech.api.management.application.internal.queryservices;

import com.agrotech.api.management.domain.model.aggregates.Enclosure;
import com.agrotech.api.management.domain.model.queries.GetAllEnclosuresByFarmerIdQuery;
import com.agrotech.api.management.domain.model.queries.GetAllEnclosuresQuery;
import com.agrotech.api.management.domain.model.queries.GetEnclosureByIdQuery;
import com.agrotech.api.management.domain.services.EnclosureQueryService;
import com.agrotech.api.management.infrastructure.persistence.jpa.mappers.EnclosureMapper;
import com.agrotech.api.management.infrastructure.persistence.jpa.repositories.EnclosureRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EnclosureQueryServiceImpl implements EnclosureQueryService {
    private final EnclosureRepository enclosureRepository;

    public EnclosureQueryServiceImpl(EnclosureRepository enclosureRepository) {
        this.enclosureRepository = enclosureRepository;
    }

    @Override
    public List<Enclosure> handle(GetAllEnclosuresQuery query) {
        return this.enclosureRepository.findAll()
                .stream()
                .map(EnclosureMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Enclosure> handle(GetEnclosureByIdQuery query) {
        return this.enclosureRepository.findById(query.enclosureId())
                .map(EnclosureMapper::toDomain);
    }

    @Override
    public List<Enclosure> handle(GetAllEnclosuresByFarmerIdQuery query) {
        return this.enclosureRepository.findAllByFarmer_Id(query.farmerId())
                .stream()
                .map(EnclosureMapper::toDomain)
                .collect(Collectors.toList());
    }
}
