package com.agrotech.api.appointment.domain.exceptions;

public class InvalidTimeRangeException extends RuntimeException {
    public InvalidTimeRangeException(String startTime, String endTime) {
        super("Invalid time range: " + startTime + " to " + endTime + ". Start time must be before end time.");
    }
}
