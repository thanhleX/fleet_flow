package com.example.fleetflowbe.service;

import com.example.fleetflowbe.common.constants.ShipmentStatus;
import com.example.fleetflowbe.common.exception.ResourceNotFoundException;
import com.example.fleetflowbe.dto.response.DeliveryAttemptResponse;
import com.example.fleetflowbe.dto.response.ShipmentTrackingResponse;
import com.example.fleetflowbe.entity.DeliveryAttempt;
import com.example.fleetflowbe.entity.Shipment;
import com.example.fleetflowbe.repository.DeliveryAttemptRepository;
import com.example.fleetflowbe.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final ShipmentRepository shipmentRepository;
    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final DeliveryAttemptService deliveryAttemptService;

    @Transactional(readOnly = true)
    public ShipmentTrackingResponse getPublicTracking(String trackingCode) {
        Shipment shipment = shipmentRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn vận chuyển", "trackingCode", trackingCode));

        List<DeliveryAttempt> attempts = deliveryAttemptRepository.findByShipmentIdOrderByAttemptNumberAsc(shipment.getId());
        List<DeliveryAttemptResponse> attemptResponses = attempts.stream()
                .map(deliveryAttemptService::mapToResponse)
                .toList();

        // Xây dựng timeline hành trình
        List<ShipmentTrackingResponse.TrackingEventDto> timeline = new ArrayList<>();
        timeline.add(ShipmentTrackingResponse.TrackingEventDto.builder()
                .title("Đơn hàng được khởi tạo")
                .description("Người gửi đã tạo đơn trên hệ thống FleetFlow")
                .timestamp(shipment.getCreatedAt())
                .build());

        if (shipment.getStatus() != ShipmentStatus.PENDING) {
            timeline.add(ShipmentTrackingResponse.TrackingEventDto.builder()
                    .title("Đã phân công tài xế")
                    .description(shipment.getAssignedDriver() != null && shipment.getAssignedDriver().getUser() != null ?
                            "Tài xế tiếp nhận: " + shipment.getAssignedDriver().getUser().getFullName() : "Đã gán tài xế")
                    .timestamp(shipment.getUpdatedAt())
                    .build());
        }

        // Thêm các sự kiện lần giao nếu có
        for (DeliveryAttempt att : attempts) {
            timeline.add(ShipmentTrackingResponse.TrackingEventDto.builder()
                    .title("Lần giao " + att.getAttemptNumber() + ": " + att.getStatus())
                    .description(att.getFailureReason() != null ? "Lý do: " + att.getFailureReason() + " - " + att.getFailureNote() : "Giao hàng thành công")
                    .timestamp(att.getAttemptedAt())
                    .build());
        }

        // Tọa độ tài xế nếu đang đi giao
        Double driverLat = null;
        Double driverLng = null;
        if (shipment.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY && shipment.getAssignedDriver() != null) {
            driverLat = shipment.getAssignedDriver().getCurrentLat();
            driverLng = shipment.getAssignedDriver().getCurrentLng();
        }

        return ShipmentTrackingResponse.builder()
                .trackingCode(shipment.getTrackingCode())
                .currentStatus(shipment.getStatus())
                .paymentStatus(shipment.getPaymentStatus())
                .serviceTier(shipment.getServiceTier())
                .senderMasked(maskNameAndPhone(shipment.getSenderName(), shipment.getSenderPhone()))
                .receiverMasked(maskNameAndPhone(shipment.getReceiverName(), shipment.getReceiverPhone()))
                .originHubName(shipment.getOriginHub() != null ? shipment.getOriginHub().getName() : "Kho gốc")
                .destinationHubName(shipment.getDestinationHub() != null ? shipment.getDestinationHub().getName() : null)
                .totalWeightKg(shipment.getTotalWeightKg())
                .codAmount(shipment.getCodAmount())
                .attemptCount(shipment.getDeliveryAttemptCount())
                .createdAt(shipment.getCreatedAt())
                .lastUpdatedAt(shipment.getUpdatedAt())
                .driverCurrentLat(driverLat)
                .driverCurrentLng(driverLng)
                .attemptHistory(attemptResponses)
                .timeline(timeline)
                .build();
    }

    private String maskNameAndPhone(String name, String phone) {
        String maskedPhone = (phone != null && phone.length() >= 7) ?
                phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3) : "***";
        return name + " (" + maskedPhone + ")";
    }
}

