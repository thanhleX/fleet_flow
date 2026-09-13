package com.example.fleetflowbe.repository;

import com.example.fleetflowbe.common.constants.CodStatus;
import com.example.fleetflowbe.entity.CodTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CodTransactionRepository extends JpaRepository<CodTransaction, Long> {

    Optional<CodTransaction> findByShipmentId(Long shipmentId);

    List<CodTransaction> findByDriverIdAndStatus(Long driverId, CodStatus status);

    List<CodTransaction> findBySettlementId(Long settlementId);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CodTransaction c WHERE c.driver.id = :driverId AND c.status = :status")
    BigDecimal sumAmountByDriverAndStatus(@Param("driverId") Long driverId, @Param("status") CodStatus status);

    @Query("SELECT c FROM CodTransaction c WHERE c.status = 'COLLECTED' AND c.collectedAt < :threshold")
    List<CodTransaction> findUnsubmittedCodTransactions(@Param("threshold") java.time.LocalDateTime threshold);
}

