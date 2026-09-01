package com.kodo.worker.application.exceptions;

public class InvalidEventException extends RuntimeException{

    public InvalidEventException(String message) {
        super(message);
    }
}
