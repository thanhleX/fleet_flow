package com.example.fleetflowbe.repository;

import com.example.fleetflowbe.common.constants.DriverStatus;
import com.example.fleetflowbe.entity.Driver;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByUserId(Long userId);

    List<Driver> findByStatus(DriverStatus status);

    List<Driver> findByCurrentHubIdAndStatus(Long hubId, DriverStatus status);

    /**
     * Pessimistic lock to prevent race condition when evaluating driver workload.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Driver d WHERE d.id = :id")
    Optional<Driver> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.assignedDriver.id = :driverId AND s.status IN ('ASSIGNED', 'PICKED_UP', 'OUT_FOR_DELIVERY')")
    long countActiveOrdersForDriver(@Param("driverId") Long driverId);
}

