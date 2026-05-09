package com.agrotech.api.profile.domain.exceptions;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(Long id) {
        super("Notification with id " + id + " not found");
    }
}
