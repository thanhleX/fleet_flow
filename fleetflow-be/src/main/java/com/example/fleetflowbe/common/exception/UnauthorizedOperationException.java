package com.example.fleetflowbe.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedOperationException extends BusinessException {
    public UnauthorizedOperationException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN_OPERATION");
    }
}

