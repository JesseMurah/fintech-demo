package com.example.fintech_demo.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String userId, String action, String resource) {
        super("User '" + userId + "' is not authorized to " + action + " on " + resource);
    }
}
