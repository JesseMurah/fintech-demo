package com.example.fintech_demo.exception;

public class DisbursementNotFoundException extends ResourceNotFoundException {

    public DisbursementNotFoundException(String disbursementId) {
        super("Disbursement not found: " + disbursementId);
    }
}
