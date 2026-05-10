package com.agrotech.api.appointment.infrastructure.web.dto;

public record AppointmentResource(Long id,
                                  Long farmerId,
                                  AvailableDateResource availableDate,
                                  String message,
                                  String status,
                                  String meetingUrl) {
}
