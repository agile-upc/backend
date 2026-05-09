package com.agrotech.api.management.domain.exceptions;

public class IncorrectHealthStatusException extends RuntimeException {
    public IncorrectHealthStatusException(String status) {
        super("Incorrect status: " + status + ". Status should be either 'HEALTHY', 'SICK', 'DEAD' or 'UNKNOWN'");
    }
}
