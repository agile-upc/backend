package com.agrotech.api.appointment.interfaces.rest.transform;

import com.agrotech.api.appointment.domain.model.entities.AvailableDate;
import com.agrotech.api.appointment.interfaces.rest.resources.AvailableDateResource;

public class AvailableDateResourceFromEntityAssembler {
    public static AvailableDateResource toResourceFromEntity(AvailableDate entity){
        return new AvailableDateResource(
                entity.getId(),
                entity.getAdvisorId(),
                entity.getScheduledDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getAvailableDateStatus()
        );
    }
}
