package com.agilsaidov.codemasia.user.exception;

import lombok.Getter;

@Getter
public class DuplicateException extends RuntimeException{

    private final String error;

    public DuplicateException(String error, String message){
        super(message);
        this.error = error;
    }
}
