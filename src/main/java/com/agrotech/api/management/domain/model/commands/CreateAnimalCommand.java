package com.agrotech.api.management.domain.model.commands;

public record CreateAnimalCommand(
        String name,
        Integer age,
        String species,
        String breed,
        Boolean gender,
        Float weight,
        String health,
        Long enclosureId
) {}

