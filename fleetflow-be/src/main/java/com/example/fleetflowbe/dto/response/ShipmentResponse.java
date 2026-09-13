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
public class ShipmentResponse {

    private Long id;
    private String trackingCode;
    private String senderName;
    private String senderPhone;
    private String senderAddress;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Long originHubId;
    private String originHubName;
    private Long destinationHubId;
    private String destinationHubName;
    private Long assignedDriverId;
    private String assignedDriverName;
    private ServiceTier serviceTier;
    private Double totalWeightKg;
    private Double volumetricWeightKg;
    private BigDecimal declaredValue;
    private BigDecimal shippingFee;
    private BigDecimal codAmount;
    private PaymentStatus paymentStatus;
    private ShipmentStatus status;
    private Integer deliveryAttemptCount;
    private Long version;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ItemDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDto {
        private Long id;
        private String itemName;
        private Integer quantity;
        private Double weightKg;
        private BigDecimal declaredPrice;
    }
}

