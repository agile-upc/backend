package com.agrotech.api.appointment.infrastructure.web.dto;

import java.time.LocalDate;

public record UpdateAvailableDateResource(LocalDate scheduledDate,
                                          String startTime,
                                          String endTime) {
}
