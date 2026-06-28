package com.library.book_service.exception;

public class ResourceNotFoundException
        extends RuntimeException {

    public ResourceNotFoundException(
            String message
    ) {
        super(message);
    }
}