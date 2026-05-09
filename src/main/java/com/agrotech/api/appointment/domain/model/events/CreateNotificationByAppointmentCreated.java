package com.agrotech.api.appointment.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CreateNotificationByAppointmentCreated extends ApplicationEvent {

    private final Long farmerId;
    private final Long availableDateId;

    public CreateNotificationByAppointmentCreated(Object source, Long farmerId, Long availableDateId) {
        super(source);
        this.farmerId = farmerId;
        this.availableDateId = availableDateId;
    }

}

