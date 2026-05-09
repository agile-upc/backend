package com.agrotech.api.management.interfaces.rest.resources;

public record AnimalResource(
        Long id,
        String name,
        Integer age,
        String species,
        String breed,
        Boolean gender,
        Float weight,
        String health,
        Long enclosureId
) {}

