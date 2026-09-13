package com.example.fleetflowbe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodSettlementRequest {

    @NotNull(message = "ID tài xế không được để trống")
    private Long driverId;

    @NotBlank(message = "Idempotency-Key không được để trống để đảm bảo an toàn giao dịch")
    private String idempotencyKey;

    private String note;
}

