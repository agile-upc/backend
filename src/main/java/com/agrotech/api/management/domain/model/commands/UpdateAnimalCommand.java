package com.agrotech.api.management.domain.model.commands;

public record UpdateAnimalCommand(
        Long animalId,
        String name,
        Integer age,
        String species,
        String breed,
        Boolean gender,
        Float weight,
        String health
) {}
