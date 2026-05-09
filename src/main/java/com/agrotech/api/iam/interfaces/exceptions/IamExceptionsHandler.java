package com.agrotech.api.iam.interfaces.exceptions;

import com.agrotech.api.iam.domain.exceptions.InvalidPasswordException;
import com.agrotech.api.iam.domain.exceptions.InvalidRoleException;
import com.agrotech.api.iam.domain.exceptions.UserNotFoundInSignInException;
import com.agrotech.api.iam.domain.exceptions.UsernameAlreadyExistsException;
import com.agrotech.api.shared.infrastructure.interfaces.responses.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class IamExceptionsHandler {
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Username Already Exists", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ErrorResponseDTO> handleRoleNotFoundException(InvalidRoleException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Role Not Found", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidPasswordException(InvalidPasswordException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Invalid Password", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundInSignInException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundInSignInException(UserNotFoundInSignInException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("User Not Found", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
