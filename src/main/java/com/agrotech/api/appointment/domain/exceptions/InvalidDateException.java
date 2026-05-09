package com.agrotech.api.appointment.domain.exceptions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InvalidDateException extends RuntimeException {
    public InvalidDateException(LocalDate date) {
        super("The date " + date.format(DateTimeFormatter.ofPattern("dd-MM-yy")) + " is in the past. Please provide a valid date.");
    }
}
