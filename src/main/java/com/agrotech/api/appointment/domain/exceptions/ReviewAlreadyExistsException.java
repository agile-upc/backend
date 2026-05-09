package com.agrotech.api.appointment.domain.exceptions;

public class ReviewAlreadyExistsException extends RuntimeException {
    public ReviewAlreadyExistsException(Long advisorId, Long farmerId) {
        super("Review for advisor with id " + advisorId + " and farmer with id " + farmerId + " already exists");
    }
}
