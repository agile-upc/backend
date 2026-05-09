package com.agrotech.api.post.interfaces.exceptions;

import com.agrotech.api.post.domain.exceptions.PostNotFoundException;
import com.agrotech.api.shared.infrastructure.interfaces.responses.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PostExceptionsHandler {
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handlePostNotFoundException(PostNotFoundException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Post Not Found", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
