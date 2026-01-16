package com.kabb.bloodbank.repository;

import com.kabb.bloodbank.domain.entity.User;
import com.kabb.bloodbank.domain.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByApprovalStatus(ApprovalStatus status);
}

