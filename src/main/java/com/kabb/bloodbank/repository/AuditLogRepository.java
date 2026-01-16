package com.kabb.bloodbank.repository;

import com.kabb.bloodbank.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserId(Long userId);
    List<AuditLog> findByTargetUserId(Long targetUserId);
    List<AuditLog> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<AuditLog> findByResourceTypeAndResourceId(String resourceType, Long resourceId);
}

