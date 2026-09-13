package com.example.fleetflowbe.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CodCollectedEvent {
    private final Long shipmentId;
    private final Long codTransactionId;
    private final Long driverId;
    private final BigDecimal amount;
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
}

