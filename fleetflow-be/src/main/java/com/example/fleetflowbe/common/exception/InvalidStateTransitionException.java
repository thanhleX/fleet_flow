package com.example.fleetflowbe.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidStateTransitionException extends BusinessException {
    public InvalidStateTransitionException(String currentState, String targetState) {
        super(String.format("Không thể chuyển đổi trạng thái từ '%s' sang '%s'", currentState, targetState),
                HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_STATE_TRANSITION");
    }
}

