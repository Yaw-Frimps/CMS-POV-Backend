package com.churchmanagement.backend.exceptions;

import java.io.IOException;

public class FailedToStoreFileException extends RuntimeException {
    public FailedToStoreFileException(String message, IOException e) {
        super(message);
    }
}
