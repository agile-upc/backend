package com.agrotech.api.profile.domain.exceptions;

public class UserAlreadyUsedException extends RuntimeException {
    public UserAlreadyUsedException(Long userId) {
        super("User with ID " + userId + " is already being used.");
    }
}
