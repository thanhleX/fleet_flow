package com.example.fleetflowbe.service;

import com.example.fleetflowbe.common.constants.CodStatus;
import com.example.fleetflowbe.common.constants.DeliveryAttemptStatus;
import com.example.fleetflowbe.common.constants.PaymentStatus;
import com.example.fleetflowbe.common.constants.ShipmentStatus;
import com.example.fleetflowbe.common.exception.BusinessException;
import com.example.fleetflowbe.common.exception.ResourceNotFoundException;
import com.example.fleetflowbe.dto.request.DeliveryAttemptRequest;
import com.example.fleetflowbe.dto.response.DeliveryAttemptResponse;
import com.example.fleetflowbe.entity.CodTransaction;
import com.example.fleetflowbe.entity.DeliveryAttempt;
import com.example.fleetflowbe.entity.Shipment;
import com.example.fleetflowbe.event.CodCollectedEvent;
import com.example.fleetflowbe.event.DeliveryFailedEvent;
import com.example.fleetflowbe.event.ShipmentDeliveredEvent;
import com.example.fleetflowbe.repository.CodTransactionRepository;
import com.example.fleetflowbe.repository.DeliveryAttemptRepository;
import com.example.fleetflowbe.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryAttemptService {

    private final ShipmentRepository shipmentRepository;
    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final CodTransactionRepository codTransactionRepository;
    private final AuditLogService auditLogService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${fleetflow.monitoring.max-delivery-attempts:3}")
    private int maxDeliveryAttempts;

    @Transactional
    public DeliveryAttemptResponse recordAttempt(Long shipmentId, DeliveryAttemptRequest request) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn vận chuyển", "id", shipmentId));

        if (shipment.getStatus() != ShipmentStatus.OUT_FOR_DELIVERY &&
                shipment.getStatus() != ShipmentStatus.DELIVERY_FAILED) {
            throw new BusinessException("Chỉ có thể ghi nhận kết quả giao hàng khi đơn đang trong chuyến giao (OUT_FOR_DELIVERY/DELIVERY_FAILED)");
        }

        if (shipment.getAssignedDriver() == null) {
            throw new BusinessException("Đơn hàng chưa được chỉ định tài xế phụ trách");
        }

        int currentAttempt = shipment.getDeliveryAttemptCount() + 1;

        DeliveryAttempt attempt = DeliveryAttempt.builder()
                .shipment(shipment)
                .driver(shipment.getAssignedDriver())
                .attemptNumber(currentAttempt)
                .status(request.getStatus())
                .failureReason(request.getFailureReason())
                .failureNote(request.getFailureNote())
                .proofImageUrl(request.getProofImageUrl())
                .attemptedAt(LocalDateTime.now())
                .rescheduledDate(request.getRescheduledDate())
                .build();

        DeliveryAttempt savedAttempt = deliveryAttemptRepository.save(attempt);
        shipment.setDeliveryAttemptCount(currentAttempt);

        log.info("[DELIVERY-ATTEMPT] Tài xế ID #{} ghi nhận kết quả giao hàng cho đơn #{}: Kết quả={}, Lần={}",
                shipment.getAssignedDriver().getId(), shipmentId, request.getStatus(), currentAttempt);

        if (request.getStatus() == DeliveryAttemptStatus.SUCCESS) {
            shipment.setStatus(ShipmentStatus.DELIVERED);

            // Xử lý tiền thu hộ COD nếu có
            if (shipment.getCodAmount() != null && shipment.getCodAmount().compareTo(BigDecimal.ZERO) > 0) {
                CodTransaction cod = codTransactionRepository.findByShipmentId(shipment.getId())
                        .orElseGet(() -> CodTransaction.builder()
                                .shipment(shipment)
                                .amount(shipment.getCodAmount())
                                .build());

                cod.setStatus(CodStatus.COLLECTED);
                cod.setDriver(shipment.getAssignedDriver());
                cod.setCollectedAt(LocalDateTime.now());
                codTransactionRepository.save(cod);

                shipment.setPaymentStatus(PaymentStatus.PAID);

                log.info("[DELIVERY-COD-COLLECTED] Thu hộ COD thành công: Đơn #{}, Số tiền: {} VNĐ, Tài xế #{}",
                        shipment.getId(), cod.getAmount(), shipment.getAssignedDriver().getId());

                eventPublisher.publishEvent(CodCollectedEvent.builder()
                        .shipmentId(shipment.getId())
                        .codTransactionId(cod.getId())
                        .driverId(shipment.getAssignedDriver().getId())
                        .amount(cod.getAmount())
                        .build());
            }

            auditLogService.record("DELIVERY_SUCCESS", "SHIPMENT", String.valueOf(shipment.getId()),
                    "{\"status\":\"OUT_FOR_DELIVERY\"}",
                    String.format("{\"status\":\"DELIVERED\",\"attempt\":%d}", currentAttempt));

            eventPublisher.publishEvent(ShipmentDeliveredEvent.builder()
                    .shipmentId(shipment.getId())
                    .trackingCode(shipment.getTrackingCode())
                    .driverId(shipment.getAssignedDriver().getId())
                    .codAmount(shipment.getCodAmount())
                    .build());

            log.info("[DELIVERY-SUCCESS] Đơn hàng #{} ({}) đã giao thành công!", shipment.getId(), shipment.getTrackingCode());

        } else { // FAILED
            log.warn("[DELIVERY-FAIL] Đơn #{} giao thất bại lần {}/{}: Lý do='{}', Ghi chú='{}'",
                    shipment.getId(), currentAttempt, maxDeliveryAttempts, request.getFailureReason(), request.getFailureNote());

            if (currentAttempt >= maxDeliveryAttempts) {
                log.warn("[DELIVERY-RETURNED] Đơn #{} vượt quá số lần giao tối đa ({}) -> Chuyển sang hoàn hàng RETURNED",
                        shipment.getTrackingCode(), maxDeliveryAttempts);
                shipment.setStatus(ShipmentStatus.RETURNED);
            } else {
                shipment.setStatus(ShipmentStatus.DELIVERY_FAILED);
            }

            auditLogService.record("DELIVERY_FAILED", "SHIPMENT", String.valueOf(shipment.getId()),
                    "{\"status\":\"OUT_FOR_DELIVERY\"}",
                    String.format("{\"status\":\"%s\",\"attempt\":%d,\"reason\":\"%s\"}",
                            shipment.getStatus(), currentAttempt, request.getFailureReason()));

            eventPublisher.publishEvent(DeliveryFailedEvent.builder()
                    .shipmentId(shipment.getId())
                    .trackingCode(shipment.getTrackingCode())
                    .driverId(shipment.getAssignedDriver().getId())
                    .attemptNumber(currentAttempt)
                    .failureReason(request.getFailureReason())
                    .failureNote(request.getFailureNote())
                    .build());
        }

        shipmentRepository.save(shipment);
        return mapToResponse(savedAttempt);
    }

    @Transactional(readOnly = true)
    public List<DeliveryAttemptResponse> getAttemptsForShipment(Long shipmentId) {
        return deliveryAttemptRepository.findByShipmentIdOrderByAttemptNumberAsc(shipmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DeliveryAttemptResponse mapToResponse(DeliveryAttempt a) {
        return DeliveryAttemptResponse.builder()
                .id(a.getId())
                .attemptNumber(a.getAttemptNumber())
                .status(a.getStatus())
                .failureReason(a.getFailureReason())
                .failureNote(a.getFailureNote())
                .proofImageUrl(a.getProofImageUrl())
                .driverId(a.getDriver() != null ? a.getDriver().getId() : null)
                .driverName(a.getDriver() != null && a.getDriver().getUser() != null ?
                        a.getDriver().getUser().getFullName() : null)
                .attemptedAt(a.getAttemptedAt())
                .rescheduledDate(a.getRescheduledDate())
                .build();
    }
}

