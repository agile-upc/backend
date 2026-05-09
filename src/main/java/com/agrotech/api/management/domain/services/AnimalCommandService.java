package com.agrotech.api.management.domain.services;

import com.agrotech.api.management.domain.model.commands.CreateAnimalCommand;
import com.agrotech.api.management.domain.model.commands.DeleteAnimalCommand;
import com.agrotech.api.management.domain.model.commands.UpdateAnimalCommand;
import com.agrotech.api.management.domain.model.entities.Animal;

import java.util.Optional;

public interface AnimalCommandService {
    Long handle(CreateAnimalCommand command);
    Optional<Animal> handle(UpdateAnimalCommand command);
    void handle(DeleteAnimalCommand command);
}
