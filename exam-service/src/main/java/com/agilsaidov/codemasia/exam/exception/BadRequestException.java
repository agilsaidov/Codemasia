package com.agilsaidov.codemasia.exam.exception;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BadRequestException extends RuntimeException {
    private final String error;

    public BadRequestException(String error, String message) {
        super(message);
        this.error = error;
    }
}
