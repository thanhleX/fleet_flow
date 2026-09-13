package com.example.fleetflowbe.scheduler;

import com.example.fleetflowbe.common.constants.ShipmentStatus;
import com.example.fleetflowbe.entity.CodTransaction;
import com.example.fleetflowbe.entity.Shipment;
import com.example.fleetflowbe.repository.CodTransactionRepository;
import com.example.fleetflowbe.repository.ShipmentRepository;
import com.example.fleetflowbe.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentMonitoringScheduler {

    private final ShipmentRepository shipmentRepository;
    private final CodTransactionRepository codTransactionRepository;
    private final AuditLogService auditLogService;

    @Value("${fleetflow.monitoring.stale-hours-threshold:24}")
    private int staleHoursThreshold;

    @Value("${fleetflow.monitoring.max-delivery-attempts:3}")
    private int maxDeliveryAttempts;

    /**
     * Tác vụ định kỳ 1: Quét các đơn hàng bị treo quá lâu không có tiến trình
     * Chạy mỗi 30 phút một lần.
     */
    @Scheduled(fixedRate = 1800000, initialDelay = 60000)
    @Transactional
    public void scanStaleShipments() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(staleHoursThreshold);
        List<Shipment> staleShipments = shipmentRepository.findStaleShipments(threshold);

        if (!staleShipments.isEmpty()) {
            log.warn("[SCHEDULER] Phát hiện {} đơn hàng bị treo quá {} giờ không có tiến trình:",
                    staleShipments.size(), staleHoursThreshold);
            for (Shipment s : staleShipments) {
                log.warn("   -> Đơn {} (Trạng thái: {}, Tạo lúc: {})",
                        s.getTrackingCode(), s.getStatus(), s.getCreatedAt());
            }
        }
    }

    /**
     * Tác vụ định kỳ 2: Tự động chuyển đơn giao thất bại vượt quá số lần cho phép sang RETURNED
     * Chạy mỗi 15 phút.
     */
    @Scheduled(fixedRate = 900000, initialDelay = 30000)
    @Transactional
    public void autoProcessExceededFailedDeliveries() {
        List<Shipment> failedList = shipmentRepository.findShipmentsExceedingMaxAttempts(maxDeliveryAttempts);
        for (Shipment s : failedList) {
            log.info("[SCHEDULER] Tự động chuyển đơn {} sang RETURNED do vượt quá {} lần giao thất bại",
                    s.getTrackingCode(), maxDeliveryAttempts);
            s.setStatus(ShipmentStatus.RETURNED);
            shipmentRepository.save(s);

            auditLogService.record("AUTO_TRANSITION_RETURNED", "SHIPMENT", String.valueOf(s.getId()),
                    "{\"status\":\"DELIVERY_FAILED\"}", "{\"status\":\"RETURNED\"}");
        }
    }

    /**
     * Tác vụ định kỳ 3: Cảnh báo các giao dịch COD tài xế đã thu nhưng chưa nộp về kho sau 24h
     * Chạy mỗi giờ một lần.
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 120000)
    @Transactional(readOnly = true)
    public void alertUnsubmittedCod() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<CodTransaction> pendingList = codTransactionRepository.findUnsubmittedCodTransactions(threshold);

        if (!pendingList.isEmpty()) {
            log.warn("[SCHEDULER] Có {} giao dịch COD đã thu tiền hơn 24 giờ nhưng tài xế chưa nộp đối soát:",
                    pendingList.size());
            for (CodTransaction cod : pendingList) {
                log.warn("   -> Đơn: {}, Tài xế ID: {}, Số tiền: {}, Thu lúc: {}",
                        cod.getShipment().getTrackingCode(),
                        cod.getDriver() != null ? cod.getDriver().getId() : "N/A",
                        cod.getAmount(),
                        cod.getCollectedAt());
            }
        }
    }
}

