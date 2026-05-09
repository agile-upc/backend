package com.agrotech.api.iam.domain.exceptions;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException() { super("Username already exists"); }
}
