package com.example.fleetflowbe.repository;

import com.example.fleetflowbe.entity.CodSettlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodSettlementRepository extends JpaRepository<CodSettlement, Long> {

    Optional<CodSettlement> findByIdempotencyKey(String idempotencyKey);

    Optional<CodSettlement> findBySettlementCode(String settlementCode);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Page<CodSettlement> findByDriverId(Long driverId, Pageable pageable);
}

