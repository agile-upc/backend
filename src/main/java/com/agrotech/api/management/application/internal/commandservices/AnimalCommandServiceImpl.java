package com.agrotech.api.management.application.internal.commandservices;

import com.agrotech.api.management.domain.exceptions.AnimalNotFoundException;
import com.agrotech.api.management.domain.exceptions.EnclosureNotFoundException;
import com.agrotech.api.management.domain.exceptions.IncorrectHealthStatusException;
import com.agrotech.api.management.domain.model.commands.CreateAnimalCommand;
import com.agrotech.api.management.domain.model.commands.DeleteAnimalCommand;
import com.agrotech.api.management.domain.model.commands.UpdateAnimalCommand;
import com.agrotech.api.management.domain.model.entities.Animal;
import com.agrotech.api.management.domain.services.AnimalCommandService;
import com.agrotech.api.management.infrastructure.persistence.jpa.mappers.AnimalMapper;
import com.agrotech.api.management.infrastructure.persistence.jpa.mappers.EnclosureMapper;
import com.agrotech.api.management.infrastructure.persistence.jpa.repositories.AnimalRepository;
import com.agrotech.api.management.infrastructure.persistence.jpa.repositories.EnclosureRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AnimalCommandServiceImpl implements AnimalCommandService {
    private final AnimalRepository animalRepository;
    private final EnclosureRepository enclosureRepository;

    public AnimalCommandServiceImpl(AnimalRepository animalRepository, EnclosureRepository enclosureRepository) {
        this.animalRepository = animalRepository;
        this.enclosureRepository = enclosureRepository;
    }

    @Override
    public Long handle(CreateAnimalCommand command) {
        var enclosure = enclosureRepository.findById(command.enclosureId())
                .orElseThrow(() -> new EnclosureNotFoundException(command.enclosureId()));
        var animal = Animal.create(command, EnclosureMapper.toDomain(enclosure));
        var animalEntity = animalRepository.save(AnimalMapper.toEntity(animal));
        return animalEntity.getId();
    }

    @Override
    public Optional<Animal> handle(UpdateAnimalCommand command) {
        var animalEntity = animalRepository.findById(command.animalId())
                .orElseThrow(() -> new AnimalNotFoundException(command.animalId()));
        if (command.health() != null) Animal.validateHealthStatus(command.health());
        animalEntity.update(command);
        animalRepository.save(animalEntity);
        return Optional.of(AnimalMapper.toDomain(animalEntity));
    }

    @Override
    public void handle(DeleteAnimalCommand command) {
        var animalEntity = animalRepository.findById(command.animalId())
                .orElseThrow(() -> new AnimalNotFoundException(command.animalId()));
        animalRepository.delete(animalEntity);
    }
}
