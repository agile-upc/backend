package com.agrotech.api.management.domain.exceptions;

public class EnclosureNotFoundException extends RuntimeException{
    public EnclosureNotFoundException(Long enclosureId) {
        super("Enclosure with id " + enclosureId + " not found");
    }
}
