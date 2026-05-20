package com.agilsaidov.codemasia.user.exception;

import com.agilsaidov.codemasia.user.dto.response.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException e, HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                OffsetDateTime.now(),
                HttpStatus.NOT_FOUND,
                e.getError(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }


    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ExceptionResponse> handleForbiddenException(ForbiddenException e, HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                OffsetDateTime.now(),
                HttpStatus.FORBIDDEN,
                e.getError(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exceptionResponse);
    }

    
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequestException(BadRequestException e, HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST,
                e.getError(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST,
                "HTTP_MESSAGE_NOT_READABLE",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST,
                "METHOD_ARGUMENT_NOT_VALID",
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ExceptionResponse> handleDuplicateException(DuplicateException e, HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                OffsetDateTime.now(),
                HttpStatus.CONFLICT,
                e.getError(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionResponse);
    }
}
