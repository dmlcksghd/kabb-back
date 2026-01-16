package com.kabb.bloodbank.repository;

import com.kabb.bloodbank.domain.entity.License;
import com.kabb.bloodbank.domain.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {
    Optional<License> findByUserId(Long userId);
    List<License> findByApprovalStatus(ApprovalStatus status);
    long countByApprovalStatus(ApprovalStatus status);
}

