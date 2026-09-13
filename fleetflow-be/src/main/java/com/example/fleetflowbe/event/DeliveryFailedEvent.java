package com.example.fleetflowbe.event;

import com.example.fleetflowbe.common.constants.FailureReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DeliveryFailedEvent {
    private final Long shipmentId;
    private final String trackingCode;
    private final Long driverId;
    private final int attemptNumber;
    private final FailureReason failureReason;
    private final String failureNote;
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
}

