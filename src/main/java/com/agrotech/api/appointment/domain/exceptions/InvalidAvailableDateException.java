package com.agrotech.api.appointment.domain.exceptions;

public class InvalidAvailableDateException extends RuntimeException {
    public InvalidAvailableDateException(Long availableDateId) {
        super("Invalid available date with id: " + availableDateId + ". Available date is not available.");
    }
}
