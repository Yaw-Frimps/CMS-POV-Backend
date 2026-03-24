package com.churchmanagement.backend.exceptions;

public class MaximumFileSizeException extends RuntimeException {
    public MaximumFileSizeException(String message) {
        super(message);
    }
}
