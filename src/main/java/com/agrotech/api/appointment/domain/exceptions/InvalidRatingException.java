package com.agrotech.api.appointment.domain.exceptions;

public class InvalidRatingException extends RuntimeException {
    public InvalidRatingException(Integer rating) {
        super("Rating must be between 0 and 5, but was " + rating);
    }
}
