package com.library.book_service.exception;

public class RemoteServiceException
        extends RuntimeException {

    public RemoteServiceException(
            String message
    ) {
        super(message);
    }
}