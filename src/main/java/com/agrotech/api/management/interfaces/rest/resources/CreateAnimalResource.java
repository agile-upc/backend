package com.agrotech.api.management.interfaces.rest.resources;

public record CreateAnimalResource (
        String name,
        Integer age,
        String species,
        String breed,
        Boolean gender,
        Float weight,
        String health,
        Long enclosureId
) {}
