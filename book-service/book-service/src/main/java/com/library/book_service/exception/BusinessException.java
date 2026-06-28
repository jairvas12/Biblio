package com.library.book_service.exception;

public class BusinessException
        extends RuntimeException {

    public BusinessException(
            String message
    ) {
        super(message);
    }
}