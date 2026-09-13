package com.example.fleetflowbe.service;

import com.example.fleetflowbe.common.exception.ResourceNotFoundException;
import com.example.fleetflowbe.dto.request.DriverLocationPingRequest;
import com.example.fleetflowbe.entity.Driver;
import com.example.fleetflowbe.entity.DriverLocation;
import com.example.fleetflowbe.repository.DriverLocationRepository;
import com.example.fleetflowbe.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverLocationService {

    private final DriverRepository driverRepository;
    private final DriverLocationRepository driverLocationRepository;

    @Transactional
    public void recordLocationPing(Long driverId, DriverLocationPingRequest request) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài xế", "id", driverId));

        LocalDateTime now = LocalDateTime.now();

        // Cập nhật vị trí tức thời trên Driver entity
        driver.setCurrentLat(request.getLatitude());
        driver.setCurrentLng(request.getLongitude());
        driver.setLastLocationUpdate(now);
        driverRepository.save(driver);

        // Lưu bản ghi lịch sử toạ độ
        DriverLocation location = DriverLocation.builder()
                .driver(driver)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speed(request.getSpeed())
                .recordedAt(now)
                .build();
        driverLocationRepository.save(location);
    }

    @Transactional(readOnly = true)
    public List<DriverLocation> getDriverRecentTrail(Long driverId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return driverLocationRepository.findDriverTrail(driverId, since);
    }
}

