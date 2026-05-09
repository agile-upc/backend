package com.agrotech.api.appointment.domain.model.commands;

public record UpdateAppointmentCommand(Long id,
                                       String message,
                                       String status) {
}
