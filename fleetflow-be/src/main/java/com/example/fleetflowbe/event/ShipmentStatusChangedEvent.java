package com.example.fleetflowbe.event;

import com.example.fleetflowbe.common.constants.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ShipmentStatusChangedEvent {
    private final Long shipmentId;
    private final String trackingCode;
    private final ShipmentStatus oldStatus;
    private final ShipmentStatus newStatus;
    private final Long actorId;
    private final String actorUsername;
    private final String note;
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
}

