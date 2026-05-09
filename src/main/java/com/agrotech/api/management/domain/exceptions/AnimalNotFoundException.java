package com.agrotech.api.management.domain.exceptions;

public class AnimalNotFoundException extends RuntimeException {
    public AnimalNotFoundException(Long animalId) {
        super("Animal with id " + animalId + " not found");
    }
}
