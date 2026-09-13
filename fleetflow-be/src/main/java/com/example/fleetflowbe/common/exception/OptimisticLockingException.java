package com.example.fleetflowbe.common.exception;

import org.springframework.http.HttpStatus;

public class OptimisticLockingException extends BusinessException {
    public OptimisticLockingException(String message) {
        super(message, HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION_CONFLICT");
    }
}

