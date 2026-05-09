package com.agrotech.api.appointment.domain.exceptions;

import java.time.LocalDate;

public class SameAvailableDateException extends RuntimeException {
    public SameAvailableDateException(LocalDate scheduledDate, String startTime, String endTime) {
        super(
                "There is an available date with the same date: " + scheduledDate +
                        " and time: " + startTime + " - " + endTime +
                        ". Please choose a different date or time."
        );
    }
}
