package com.agilsaidov.codemasia.user.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException e, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, e.getError(), e.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ExceptionResponse> handleForbiddenException(ForbiddenException e, HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, e.getError(), e.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequestException(BadRequestException e, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, e.getError(), e.getMessage(), request);
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ExceptionResponse> handleDuplicateException(DuplicateException e, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, e.getError(), e.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST,
                "INVALID_ARGUMENT",
                e.getMessage() != null ? e.getMessage() : "Invalid argument",
                request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e,
            HttpServletRequest request) {
        String message = "Required parameter '%s' is missing".formatted(e.getParameterName());
        return response(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", message, request);
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "ARGUMENT_TYPE_MISMATCH", e.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException e,
            HttpServletRequest request) {
        log.warn("Data integrity violation: {}", e.getMessage());
        return response(
                HttpStatus.CONFLICT,
                "USER_ALREADY_EXISTS",
                "A user with this unique field already exists",
                request);
    }

    @ExceptionHandler(WebApplicationException.class)
    public ResponseEntity<ExceptionResponse> handleWebApplicationException(
            WebApplicationException e,
            HttpServletRequest request) {
        int status = e.getResponse() != null ? e.getResponse().getStatus() : 500;
        log.warn("External service error: status={} message={}", status, e.getMessage());

        if (status == 409) {
            return response(
                    HttpStatus.CONFLICT,
                    "USER_ALREADY_EXISTS",
                    "Resource already exists",
                    request);
        }

        HttpStatus httpStatus = status >= 500 ? HttpStatus.BAD_GATEWAY : HttpStatus.BAD_REQUEST;
        String error = status >= 500 ? "EXTERNAL_SERVICE_ERROR" : "EXTERNAL_SERVICE_BAD_REQUEST";
        return response(
                httpStatus,
                error,
                "External identity service request failed",
                request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedException(
            AccessDeniedException e,
            HttpServletRequest request) {
        return response(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to access this resource",
                request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponse> handleAuthenticationException(
            AuthenticationException e,
            HttpServletRequest request) {
        return response(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Authentication required",
                request);
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

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ExceptionResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException e,
            HttpServletRequest request
    ){
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
                request
        );
    }
}
