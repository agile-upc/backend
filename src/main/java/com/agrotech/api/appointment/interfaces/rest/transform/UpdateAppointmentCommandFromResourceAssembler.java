package com.agrotech.api.appointment.interfaces.rest.transform;

import com.agrotech.api.appointment.domain.model.commands.UpdateAppointmentCommand;
import com.agrotech.api.appointment.interfaces.rest.resources.UpdateAppointmentResource;

public class UpdateAppointmentCommandFromResourceAssembler {
    public static UpdateAppointmentCommand toCommandFromResource(Long id, UpdateAppointmentResource resource){
        return new UpdateAppointmentCommand(
                id,
                resource.message(),
                resource.status()
        );
    }
}
