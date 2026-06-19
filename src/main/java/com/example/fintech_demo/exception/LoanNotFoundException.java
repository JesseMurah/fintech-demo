package com.example.fintech_demo.exception;

public class LoanNotFoundException extends ResourceNotFoundException {

    public LoanNotFoundException(String loanId) {
        super("Loan not found: " + loanId);
    }
}
