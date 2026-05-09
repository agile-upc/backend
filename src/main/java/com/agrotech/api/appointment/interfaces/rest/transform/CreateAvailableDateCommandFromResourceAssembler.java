package com.agrotech.api.appointment.interfaces.rest.transform;

import com.agrotech.api.appointment.domain.model.commands.CreateAvailableDateCommand;
import com.agrotech.api.appointment.interfaces.rest.resources.CreateAvailableDateResource;

public class CreateAvailableDateCommandFromResourceAssembler {
    public static CreateAvailableDateCommand toCommandFromResource(CreateAvailableDateResource resource){
        return new CreateAvailableDateCommand(
                resource.advisorId(),
                resource.scheduledDate(),
                resource.startTime(),
                resource.endTime()
        );
    }
}
