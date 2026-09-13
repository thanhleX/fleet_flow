package com.example.fleetflowbe.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ShipmentAssignedEvent {
    private final Long shipmentId;
    private final String trackingCode;
    private final Long driverId;
    private final String driverName;
    private final Long assignedByUserId;
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
}

