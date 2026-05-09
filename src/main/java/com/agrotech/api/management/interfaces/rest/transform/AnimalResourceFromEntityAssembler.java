package com.agrotech.api.management.interfaces.rest.transform;

import com.agrotech.api.management.domain.model.entities.Animal;
import com.agrotech.api.management.interfaces.rest.resources.AnimalResource;

public class AnimalResourceFromEntityAssembler {
    public static AnimalResource toResourceFromEntity(Animal entity) {
        return new AnimalResource(
                entity.getId(),
                entity.getName(),
                entity.getAge(),
                entity.getSpecies(),
                entity.getBreed(),
                entity.getGender(),
                entity.getWeight(),
                entity.getHealthStatus(),
                entity.getEnclosureId()
        );
    }
}
