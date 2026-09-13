package com.example.fleetflowbe.repository;

import com.example.fleetflowbe.entity.DriverLocation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {

    List<DriverLocation> findByDriverIdOrderByRecordedAtDesc(Long driverId, Pageable pageable);

    @Query("SELECT dl FROM DriverLocation dl WHERE dl.driver.id = :driverId AND dl.recordedAt >= :since ORDER BY dl.recordedAt ASC")
    List<DriverLocation> findDriverTrail(@Param("driverId") Long driverId, @Param("since") LocalDateTime since);
}

