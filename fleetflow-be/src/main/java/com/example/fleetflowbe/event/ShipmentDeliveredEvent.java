package com.example.fleetflowbe.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ShipmentDeliveredEvent {
    private final Long shipmentId;
    private final String trackingCode;
    private final Long driverId;
    private final BigDecimal codAmount;
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
}

