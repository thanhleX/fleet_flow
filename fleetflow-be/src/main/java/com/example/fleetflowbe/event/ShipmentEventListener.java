package com.example.fleetflowbe.event;

import com.example.fleetflowbe.entity.AuditLog;
import com.example.fleetflowbe.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentEventListener {

    private final AuditLogRepository auditLogRepository;

    @Async("fleetAsyncExecutor")
    @EventListener
    public void handleShipmentDelivered(ShipmentDeliveredEvent event) {
        log.info("[EVENT] Đơn hàng {} đã giao thành công bởi Tài xế ID: {}. Số tiền COD thu hộ: {}",
                event.getTrackingCode(), event.getDriverId(), event.getCodAmount());

        // Async audit recording
        AuditLog auditLog = AuditLog.builder()
                .actorId(event.getDriverId())
                .actorUsername("Driver#" + event.getDriverId())
                .actorRole("ROLE_DRIVER")
                .action("DELIVER_SHIPMENT_SUCCESS")
                .entityName("SHIPMENT")
                .entityId(String.valueOf(event.getShipmentId()))
                .newDataJson(String.format("{\"trackingCode\":\"%s\",\"status\":\"DELIVERED\",\"codAmount\":%s}",
                        event.getTrackingCode(), event.getCodAmount()))
                .performedAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }

    @Async("fleetAsyncExecutor")
    @EventListener
    public void handleDeliveryFailed(DeliveryFailedEvent event) {
        log.warn("[EVENT] Đơn hàng {} giao thất bại (Lần {}). Lý do: {}. Ghi chú: {}",
                event.getTrackingCode(), event.getAttemptNumber(), event.getFailureReason(), event.getFailureNote());

        AuditLog auditLog = AuditLog.builder()
                .actorId(event.getDriverId())
                .actorUsername("Driver#" + event.getDriverId())
                .actorRole("ROLE_DRIVER")
                .action("DELIVERY_FAILED_ATTEMPT")
                .entityName("SHIPMENT")
                .entityId(String.valueOf(event.getShipmentId()))
                .newDataJson(String.format("{\"attempt\":%d,\"reason\":\"%s\",\"note\":\"%s\"}",
                        event.getAttemptNumber(), event.getFailureReason(), event.getFailureNote()))
                .performedAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }

    @Async("fleetAsyncExecutor")
    @EventListener
    public void handleShipmentAssigned(ShipmentAssignedEvent event) {
        log.info("[EVENT] Đơn hàng {} đã được phân công cho Tài xế {}. Người phân công: {}",
                event.getTrackingCode(), event.getDriverName(), event.getAssignedByUserId());
    }

    @Async("fleetAsyncExecutor")
    @EventListener
    public void handleCodCollected(CodCollectedEvent event) {
        log.info("[EVENT] Đã thu tiền COD {} cho đơn ID {}. Tài xế phụ trách: {}",
                event.getAmount(), event.getShipmentId(), event.getDriverId());
    }
}

