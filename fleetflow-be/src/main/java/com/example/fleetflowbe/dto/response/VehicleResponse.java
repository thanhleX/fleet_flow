package com.example.fleetflowbe.dto.response;

import com.example.fleetflowbe.common.constants.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private Long id;
    private String licensePlate;
    private VehicleType vehicleType;
    private Double maxPayloadKg;
    private Double maxVolumeM3;
    private boolean active;
}

