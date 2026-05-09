package com.agrotech.api.appointment.interfaces.rest.resources;

public record CreateAppointmentResource(Long farmerId,
                                        Long availableDateId,
                                        String message) {
}
