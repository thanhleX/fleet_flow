package com.example.fleetflowbe.dto.request;

import com.example.fleetflowbe.common.constants.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShipmentStatusRequest {

    @NotNull(message = "Trạng thái mới không được để trống")
    private ShipmentStatus status;

    private String note;
}

