package com.agrotech.api.management.interfaces.exceptions;

import com.agrotech.api.management.domain.exceptions.AnimalNotFoundException;
import com.agrotech.api.management.domain.exceptions.CropNotFoundException;
import com.agrotech.api.management.domain.exceptions.EnclosureNotFoundException;
import com.agrotech.api.management.domain.exceptions.IncorrectHealthStatusException;
import com.agrotech.api.shared.infrastructure.interfaces.responses.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ManagementExceptionsHandler {
    @ExceptionHandler(AnimalNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleAnimalNotFoundException(AnimalNotFoundException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Animal Not Found", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EnclosureNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEnclosureNotFoundException(EnclosureNotFoundException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Enclosure Not Found", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IncorrectHealthStatusException.class)
    public ResponseEntity<ErrorResponseDTO> handleIncorrectHealthStatusException(IncorrectHealthStatusException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Incorrect Health Status", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CropNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCropNotFoundException(CropNotFoundException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Crop Not Found", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
