package com.agrotech.api.management.interfaces.rest.transform;

import com.agrotech.api.management.domain.model.commands.CreateAnimalCommand;
import com.agrotech.api.management.interfaces.rest.resources.CreateAnimalResource;

public class CreateAnimalCommandFromResourceAssembler {
    public static CreateAnimalCommand toCommandFromResource(CreateAnimalResource resource){
        return new CreateAnimalCommand(
                resource.name(),
                resource.age(),
                resource.species(),
                resource.breed(),
                resource.gender(),
                resource.weight(),
                resource.health(),
                resource.enclosureId()
        );
    }
}
