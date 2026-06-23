package com.library.user_service.exception;

public class EmailChangeNotAllowedException
        extends RuntimeException {

    public EmailChangeNotAllowedException(
            String message
    ) {
        super(message);
    }
}
