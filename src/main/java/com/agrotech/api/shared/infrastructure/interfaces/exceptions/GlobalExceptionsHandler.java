package com.agrotech.api.shared.infrastructure.interfaces.exceptions;

import com.agrotech.api.shared.domain.exceptions.AdvisorNotFoundException;
import com.agrotech.api.shared.domain.exceptions.FarmerNotFoundException;
import com.agrotech.api.shared.domain.exceptions.UserNotFoundException;
import com.agrotech.api.shared.infrastructure.interfaces.responses.ErrorResponseDTO;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionsHandler {
    // Maneja excepciones generales
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Ocurrió un error inesperado.";
        String stackTrace = Arrays.stream(e.getStackTrace())
                .limit(3)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
        String detailedMessage = String.format("Mensaje: %s\nTraza: %s", message, stackTrace);
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Error Interno del Servidor", detailedMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    // Jakarta Validations
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Constraint Violation", errorMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(UserNotFoundException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("User Not Found", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AdvisorNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleAdvisorNotFoundException(AdvisorNotFoundException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Advisor Not Found", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FarmerNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleFarmerNotFoundException(FarmerNotFoundException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Farmer Not Found", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
