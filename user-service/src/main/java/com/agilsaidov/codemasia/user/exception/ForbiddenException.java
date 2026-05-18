package com.agilsaidov.codemasia.user.exception;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ForbiddenException extends RuntimeException {
    private final String error;
    public ForbiddenException(String error, String message) {
        super(message);
        this.error = error;
    }
}
