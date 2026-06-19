package com.example.fintech_demo.exception;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(String userId) {
        super("User not found: " + userId);
    }
}
