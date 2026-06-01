package com.agilsaidov.codemasia.exam.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static ResponseEntity<ExceptionResponse> response(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request) {
        ExceptionResponse body = new ExceptionResponse(
                OffsetDateTime.now(),
                status,
                error,
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e,
            HttpServletRequest request) {
        log.debug("Malformed request body: {}", e.getMessage());
        return response(
                HttpStatus.BAD_REQUEST,
                "HTTP_MESSAGE_NOT_READABLE",
                "Invalid request body",
                request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Validation failed");
        return response(HttpStatus.BAD_REQUEST, "METHOD_ARGUMENT_NOT_VALID", message, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e,
            HttpServletRequest request) {
        String message = "Required parameter '%s' is missing".formatted(e.getParameterName());
        return response(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", message, request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ExceptionResponse> handleMissingRequestHeaderException(
            MissingRequestHeaderException e,
            HttpServletRequest request) {
        String message = "Required header '%s' is missing".formatted(e.getHeaderName());
        return response(HttpStatus.BAD_REQUEST, "MISSING_HEADER", message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "ARGUMENT_TYPE_MISMATCH", e.getMessage(), request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ExceptionResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException e,
            HttpServletRequest request) {
        String message = e.getParameterValidationResults().stream()
                .findFirst()
                .map(result -> result
                        .getResolvableErrors()
                        .getFirst()
                        .getDefaultMessage())
                .orElse("Validation failed");

        return response(
                (HttpStatus) e.getStatusCode(),
                "PARAMETER_VALIDATION_FAILURE",
                message,
                request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequestException(BadRequestException e, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, e.getError(), e.getMessage(), request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException e, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, e.getError(), e.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ExceptionResponse> handleForbiddenException(ForbiddenException e, HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, e.getError(), e.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "NO_RESOURCE_FOUND", "Endpoint not found", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception on {}", request.getRequestURI(), e);
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request);
    }
}
