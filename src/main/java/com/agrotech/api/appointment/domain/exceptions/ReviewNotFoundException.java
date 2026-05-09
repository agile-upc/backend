package com.agrotech.api.appointment.domain.exceptions;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(Long id) {
        super("Review with id " + id + " not found");
    }
}
