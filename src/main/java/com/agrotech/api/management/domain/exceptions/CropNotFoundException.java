package com.agrotech.api.management.domain.exceptions;

public class CropNotFoundException extends RuntimeException {
    public CropNotFoundException(Long cropId) {
        super("Crop with id " + cropId + " not found");
    }
}
