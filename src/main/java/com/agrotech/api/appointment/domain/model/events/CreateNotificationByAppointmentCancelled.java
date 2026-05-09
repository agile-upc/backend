package com.agrotech.api.appointment.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CreateNotificationByAppointmentCancelled extends ApplicationEvent {
    private final Long availableDateId;

    public CreateNotificationByAppointmentCancelled(Object source, Long availableDateId) {
        super(source);
        this.availableDateId = availableDateId;
    }
}
