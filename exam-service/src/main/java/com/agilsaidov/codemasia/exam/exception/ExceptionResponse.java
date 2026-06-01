package com.agilsaidov.codemasia.exam.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

@AllArgsConstructor
@Getter @Setter
public class ExceptionResponse {
    private OffsetDateTime timestamp;
    private HttpStatus status;
    private String error;
    private String message;
    private String path;
}
