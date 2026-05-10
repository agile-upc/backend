package com.agrotech.api.appointment.infrastructure.web.dto;

import java.time.LocalDate;

public record CreateAvailableDateResource(LocalDate scheduledDate,
                                          String startTime,
                                          String endTime) {
}
