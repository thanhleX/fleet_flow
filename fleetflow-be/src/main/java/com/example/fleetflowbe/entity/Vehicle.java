package com.example.fleetflowbe.entity;

import com.example.fleetflowbe.common.constants.VehicleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicle_plate", columnList = "license_plate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType;

    @Column(name = "max_payload_kg", nullable = false)
    private Double maxPayloadKg;

    @Column(name = "max_volume_m3", nullable = false)
    private Double maxVolumeM3;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}

