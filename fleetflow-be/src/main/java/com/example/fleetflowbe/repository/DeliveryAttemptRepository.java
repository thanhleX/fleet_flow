package com.example.fleetflowbe.repository;

import com.example.fleetflowbe.entity.DeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, Long> {

    List<DeliveryAttempt> findByShipmentIdOrderByAttemptNumberAsc(Long shipmentId);

    long countByShipmentId(Long shipmentId);
}

