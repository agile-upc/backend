package com.agrotech.api.appointment.domain.exceptions;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException() {
        super("Profile not found.");
    }
}
