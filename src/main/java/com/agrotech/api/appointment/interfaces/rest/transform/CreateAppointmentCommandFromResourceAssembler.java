package com.agrotech.api.appointment.interfaces.rest.transform;

import com.agrotech.api.appointment.domain.model.commands.CreateAppointmentCommand;
import com.agrotech.api.appointment.interfaces.rest.resources.CreateAppointmentResource;

public class CreateAppointmentCommandFromResourceAssembler {
    public static CreateAppointmentCommand toCommandFromResource(CreateAppointmentResource resource){
        return new CreateAppointmentCommand(
                resource.availableDateId(),
                resource.farmerId(),
                resource.message()
        );
    }
}
