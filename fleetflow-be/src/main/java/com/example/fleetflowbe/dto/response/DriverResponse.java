package com.example.fleetflowbe.dto.response;

import com.example.fleetflowbe.common.constants.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String phone;
    private String licenseNumber;
    private DriverStatus status;
    private Integer maxActiveOrders;
    private Long activeOrdersCount;
    private Long vehicleId;
    private String vehicleLicensePlate;
    private Double currentLat;
    private Double currentLng;
    private LocalDateTime lastLocationUpdate;
}

