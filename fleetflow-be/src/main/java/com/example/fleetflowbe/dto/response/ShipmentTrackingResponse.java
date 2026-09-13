package com.example.fleetflowbe.dto.response;

import com.example.fleetflowbe.common.constants.PaymentStatus;
import com.example.fleetflowbe.common.constants.ServiceTier;
import com.example.fleetflowbe.common.constants.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentTrackingResponse {

    private String trackingCode;
    private ShipmentStatus currentStatus;
    private PaymentStatus paymentStatus;
    private ServiceTier serviceTier;
    private String senderMasked;
    private String receiverMasked;
    private String originHubName;
    private String destinationHubName;
    private Double totalWeightKg;
    private BigDecimal codAmount;
    private Integer attemptCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

    // Driver location if currently OUT_FOR_DELIVERY
    private Double driverCurrentLat;
    private Double driverCurrentLng;

    private List<DeliveryAttemptResponse> attemptHistory;
    private List<TrackingEventDto> timeline;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingEventDto {
        private String title;
        private String description;
        private LocalDateTime timestamp;
    }
}

