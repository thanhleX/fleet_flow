package com.example.fleetflowbe.repository;

import com.example.fleetflowbe.common.constants.ShipmentStatus;
import com.example.fleetflowbe.entity.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByTrackingCode(String trackingCode);

    boolean existsByTrackingCode(String trackingCode);

    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);

    Page<Shipment> findByAssignedDriverId(Long driverId, Pageable pageable);

    List<Shipment> findByAssignedDriverIdAndStatusIn(Long driverId, Collection<ShipmentStatus> statuses);

    Page<Shipment> findByOriginHubId(Long hubId, Pageable pageable);

    @Query("SELECT s FROM Shipment s WHERE s.status = :status AND s.assignedDriver IS NULL ORDER BY s.createdAt ASC")
    List<Shipment> findUnassignedShipmentsByStatus(@Param("status") ShipmentStatus status);

    default List<Shipment> findUnassignedShipments() {
        return findUnassignedShipmentsByStatus(ShipmentStatus.PENDING);
    }

    @Query("SELECT s FROM Shipment s WHERE s.status IN :statuses AND s.createdAt < :threshold")
    List<Shipment> findStaleShipments(@Param("statuses") Collection<ShipmentStatus> statuses, @Param("threshold") LocalDateTime threshold);

    default List<Shipment> findStaleShipments(LocalDateTime threshold) {
        return findStaleShipments(List.of(ShipmentStatus.PENDING, ShipmentStatus.ASSIGNED), threshold);
    }

    @Query("SELECT s FROM Shipment s WHERE s.status = :status AND s.deliveryAttemptCount >= :maxAttempts")
    List<Shipment> findShipmentsExceedingMaxAttempts(@Param("status") ShipmentStatus status, @Param("maxAttempts") Integer maxAttempts);

    default List<Shipment> findShipmentsExceedingMaxAttempts(Integer maxAttempts) {
        return findShipmentsExceedingMaxAttempts(ShipmentStatus.DELIVERY_FAILED, maxAttempts);
    }

    // Dashboard Analytics Aggregation Queries
    @Query("SELECT s.status, COUNT(s) FROM Shipment s GROUP BY s.status")
    List<Object[]> countShipmentsGroupedByStatus();

    @Query("SELECT COALESCE(SUM(s.shippingFee), 0) FROM Shipment s WHERE s.status = :status")
    BigDecimal calculateTotalShippingRevenueByStatus(@Param("status") ShipmentStatus status);

    default BigDecimal calculateTotalDeliveredShippingRevenue() {
        return calculateTotalShippingRevenueByStatus(ShipmentStatus.DELIVERED);
    }

    @Query("SELECT COALESCE(SUM(s.codAmount), 0) FROM Shipment s WHERE s.status = :status AND s.codAmount > 0")
    BigDecimal calculateTotalCodVolumeByStatus(@Param("status") ShipmentStatus status);

    default BigDecimal calculateTotalDeliveredCodVolume() {
        return calculateTotalCodVolumeByStatus(ShipmentStatus.DELIVERED);
    }

    long countByStatus(ShipmentStatus status);

    long countByStatusIn(Collection<ShipmentStatus> statuses);
}

