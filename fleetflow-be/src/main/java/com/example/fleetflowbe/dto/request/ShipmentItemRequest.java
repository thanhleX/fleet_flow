package com.example.fleetflowbe.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentItemRequest {

    @NotBlank(message = "Tên mặt hàng không được để trống")
    private String itemName;

    @NotNull(message = "Số lượng không được để trống")
    private Integer quantity;

    @NotNull(message = "Khối lượng không được để trống")
    @DecimalMin(value = "0.01", message = "Khối lượng phải lớn hơn 0")
    private Double weightKg;

    private BigDecimal declaredPrice;
}

