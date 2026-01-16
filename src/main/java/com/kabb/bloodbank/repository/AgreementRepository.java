package com.kabb.bloodbank.repository;

import com.kabb.bloodbank.domain.entity.Agreement;
import com.kabb.bloodbank.domain.enums.AgreementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgreementRepository extends JpaRepository<Agreement, Long> {
    List<Agreement> findByUserId(Long userId);
    Optional<Agreement> findByUserIdAndAgreementType(Long userId, AgreementType agreementType);
    List<Agreement> findByUserIdAndAgreed(Long userId, Boolean agreed);
}

