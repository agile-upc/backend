package com.agrotech.api.appointment.interfaces.rest.transform;

import com.agrotech.api.appointment.domain.model.commands.UpdateAvailableDateCommand;
import com.agrotech.api.appointment.interfaces.rest.resources.UpdateAvailableDateResource;

public class UpdateAvailableDateCommandFromResourceAssembler {
    public static UpdateAvailableDateCommand toCommandFromResource(Long id, UpdateAvailableDateResource resource){
        return new UpdateAvailableDateCommand(
                id,
                resource.scheduledDate(),
                resource.startTime(),
                resource.endTime()
        );
    }
}
