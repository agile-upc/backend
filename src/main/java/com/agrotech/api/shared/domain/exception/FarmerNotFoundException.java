package com.agrotech.api.shared.domain.exception;

public class FarmerNotFoundException extends RuntimeException {
    public FarmerNotFoundException(Long id) {
        super("Farmer with id " + id + " not found");
    }
}
