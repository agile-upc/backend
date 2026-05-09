package com.agrotech.api.profile.interfaces.exceptions;

import com.agrotech.api.profile.domain.exceptions.NotificationNotFoundException;
import com.agrotech.api.profile.domain.exceptions.ProfileNotFoundException;
import com.agrotech.api.profile.domain.exceptions.UserAlreadyUsedException;
import com.agrotech.api.shared.infrastructure.interfaces.responses.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
class ProfileExceptionsHandler{
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotificationNotFoundException(NotificationNotFoundException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Notification Not Found", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleProfileNotFoundException(ProfileNotFoundException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Profile Not Found", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyUsedException.class)
    public ResponseEntity<ErrorResponseDTO> handleSameUserException(UserAlreadyUsedException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Repeated User", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
