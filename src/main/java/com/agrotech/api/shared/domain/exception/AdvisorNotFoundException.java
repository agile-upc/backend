package com.agrotech.api.shared.domain.exception;

public class AdvisorNotFoundException extends RuntimeException {
    public AdvisorNotFoundException(Long id) {
        super("Advisor with id " + id + " not found");
    }
}
