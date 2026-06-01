package com.agilsaidov.codemasia.exam.exception;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NotFoundException extends RuntimeException {
    private final String error;
    public NotFoundException(String error, String message) {
        super(message);
        this.error = error;
    }
}
