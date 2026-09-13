package com.example.fleetflowbe.repository;

import com.example.fleetflowbe.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByEntityNameAndEntityIdOrderByPerformedAtDesc(String entityName, String entityId, Pageable pageable);

    List<AuditLog> findByEntityIdOrderByPerformedAtDesc(String entityId);

    Page<AuditLog> findByActorIdOrderByPerformedAtDesc(Long actorId, Pageable pageable);
}

