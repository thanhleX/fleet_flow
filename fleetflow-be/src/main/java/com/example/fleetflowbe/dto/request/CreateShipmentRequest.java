package com.example.fleetflowbe.dto.request;

import com.example.fleetflowbe.common.constants.PaymentStatus;
import com.example.fleetflowbe.common.constants.ServiceTier;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentRequest {

    private Long senderCustomerId;

    @NotBlank(message = "Tên người gửi không được để trống")
    private String senderName;

    @NotBlank(message = "Số điện thoại người gửi không được để trống")
    private String senderPhone;

    @NotBlank(message = "Địa chỉ người gửi không được để trống")
    private String senderAddress;

    private String senderProvince;

    @NotBlank(message = "Tên người nhận không được để trống")
    private String receiverName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    private String receiverPhone;

    @NotBlank(message = "Địa chỉ người nhận không được để trống")
    private String receiverAddress;

    private String receiverProvince;

    @NotNull(message = "Kho gửi (origin hub) không được để trống")
    private Long originHubId;

    private Long destinationHubId;

    @Builder.Default
    private ServiceTier serviceTier = ServiceTier.STANDARD;

    @NotNull(message = "Tổng trọng lượng thực tế không được để trống")
    @DecimalMin(value = "0.05", message = "Khối lượng tối thiểu 0.05kg")
    private Double totalWeightKg;

    private Double lengthCm;
    private Double widthCm;
    private Double heightCm;

    private BigDecimal declaredValue;

    @Builder.Default
    private BigDecimal codAmount = BigDecimal.ZERO;

    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private String note;

    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 mặt hàng chi tiết")
    @Valid
    private List<ShipmentItemRequest> items;
}

