package com.example.fleetflowbe.entity;

import com.example.fleetflowbe.common.constants.DriverStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "drivers", indexes = {
        @Index(name = "idx_driver_status", columnList = "status"),
        @Index(name = "idx_driver_hub", columnList = "current_hub_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_hub_id")
    private Warehouse currentHub;

    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private DriverStatus status = DriverStatus.AVAILABLE;

    @Builder.Default
    @Column(name = "max_active_orders", nullable = false)
    private Integer maxActiveOrders = 10;

    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lng")
    private Double currentLng;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;
}

