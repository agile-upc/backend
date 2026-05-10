package com.agrotech.api.appointment.infrastructure.web.dto;

import java.time.LocalDate;

public record AvailableDateResource(Long id,
                                    Long advisorId,
                                    LocalDate scheduledDate,
                                    String startTime,
                                    String endTime,
                                    String status) {
}
