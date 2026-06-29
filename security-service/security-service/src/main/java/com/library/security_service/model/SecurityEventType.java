package com.library.security_service.model;

public enum SecurityEventType {

    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    ACCESS_DENIED,
    INVALID_TOKEN,
    ROLE_CHANGE,
    USER_BLOCKED,
    ADMIN_ACTION
}