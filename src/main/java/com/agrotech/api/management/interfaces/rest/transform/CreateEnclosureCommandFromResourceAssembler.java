package com.agrotech.api.management.interfaces.rest.transform;

import com.agrotech.api.management.domain.model.commands.CreateEnclosureCommand;
import com.agrotech.api.management.interfaces.rest.resources.CreateEnclosureResource;

public class CreateEnclosureCommandFromResourceAssembler {
    public static CreateEnclosureCommand toCommandFromResource(CreateEnclosureResource resource){
        return new CreateEnclosureCommand(
                resource.name(),
                resource.capacity(),
                resource.type(),
                resource.farmerId()
        );
    }
}
