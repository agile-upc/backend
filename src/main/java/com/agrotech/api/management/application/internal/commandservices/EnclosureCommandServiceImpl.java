package com.agrotech.api.management.application.internal.commandservices;

import com.agrotech.api.management.domain.exceptions.EnclosureNotFoundException;
import com.agrotech.api.management.domain.model.aggregates.Enclosure;
import com.agrotech.api.management.domain.model.commands.CreateEnclosureCommand;
import com.agrotech.api.management.domain.model.commands.DeleteEnclosureCommand;
import com.agrotech.api.management.domain.model.commands.UpdateEnclosureCommand;
import com.agrotech.api.management.domain.services.EnclosureCommandService;
import com.agrotech.api.management.infrastructure.persistence.jpa.mappers.EnclosureMapper;
import com.agrotech.api.management.infrastructure.persistence.jpa.repositories.EnclosureRepository;
import com.agrotech.api.profile.infrastructure.persistence.jpa.mappers.FarmerMapper;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repositories.FarmerRepository;
import com.agrotech.api.shared.domain.exceptions.FarmerNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EnclosureCommandServiceImpl implements EnclosureCommandService {
    private final EnclosureRepository enclosureRepository;
    private final FarmerRepository farmerRepository;

    public EnclosureCommandServiceImpl(EnclosureRepository enclosureRepository, FarmerRepository farmerRepository) {
        this.enclosureRepository = enclosureRepository;
        this.farmerRepository = farmerRepository;
    }

    @Override
    public Long handle(CreateEnclosureCommand command) {
        var farmer = farmerRepository.findById(command.farmerId())
                .orElseThrow(() -> new FarmerNotFoundException(command.farmerId()));
        var enclosure = new Enclosure(command, FarmerMapper.toDomain(farmer));
        var enclosureEntity = enclosureRepository.save(EnclosureMapper.toEntity(enclosure));
        return enclosureEntity.getId();
    }

    @Override
    public Optional<Enclosure> handle(UpdateEnclosureCommand command) {
        var enclosureEntity = enclosureRepository.findById(command.enclosureId())
                .orElseThrow(() -> new EnclosureNotFoundException(command.enclosureId()));
        enclosureEntity.update(command);
        enclosureRepository.save(enclosureEntity);
        return Optional.of(EnclosureMapper.toDomain(enclosureEntity));
    }

    @Override
    public void handle(DeleteEnclosureCommand command) {
        var enclosure = enclosureRepository.findById(command.enclosureId())
                .orElseThrow(() -> new EnclosureNotFoundException(command.enclosureId()));
        enclosureRepository.delete(enclosure);
    }
}
