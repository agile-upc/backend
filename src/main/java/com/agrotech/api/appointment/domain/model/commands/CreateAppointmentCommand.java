package com.agrotech.api.appointment.domain.model.commands;

public record CreateAppointmentCommand(Long availableDateId,
                                       Long farmerId,
                                       String message) {
}
