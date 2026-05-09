package com.agrotech.api.appointment.interfaces.rest.resources;

public record AppointmentResource(Long id,
                                  Long farmerId,
                                  Long availableDateId,
                                  String message,
                                  String status,
                                  String meetingUrl) {
}
