package com.kabb.bloodbank.service;

import com.kabb.bloodbank.domain.entity.License;
import com.kabb.bloodbank.domain.entity.User;
import com.kabb.bloodbank.domain.enums.ApprovalStatus;
import com.kabb.bloodbank.domain.enums.AuditActionType;
import com.kabb.bloodbank.dto.response.LicenseApprovalResponse;
import com.kabb.bloodbank.repository.LicenseRepository;
import com.kabb.bloodbank.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LicenseApprovalService {

    private final LicenseRepository licenseRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    /**
     * 면허증 승인 대기 목록 조회
     */
    public List<LicenseApprovalResponse> getPendingLicenses() {
        return licenseRepository.findByApprovalStatus(ApprovalStatus.PENDING)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 면허증 승인
     */
    @Transactional
    public LicenseApprovalResponse approveLicense(Long licenseId, Long adminId, HttpServletRequest request) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new IllegalArgumentException("면허증을 찾을 수 없습니다"));

        if (license.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalArgumentException("승인 대기 상태의 면허증만 승인할 수 있습니다");
        }

        license.setApprovalStatus(ApprovalStatus.APPROVED);
        license.setApprovedBy(adminId);
        license.setApprovedAt(LocalDateTime.now());
        license.setRejectionReason(null);
        licenseRepository.save(license);

        // 사용자 승인 상태 업데이트
        User user = license.getUser();
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        userRepository.save(user);

        // 감사 로그 기록
        auditLogService.log(
                AuditActionType.APPROVE,
                "LICENSE",
                licenseId,
                adminId,
                user.getId(),
                "면허증 승인 완료",
                request
        );

        return convertToResponse(license);
    }

    /**
     * 면허증 거절
     */
    @Transactional
    public LicenseApprovalResponse rejectLicense(Long licenseId, Long adminId, String reason, HttpServletRequest request) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new IllegalArgumentException("면허증을 찾을 수 없습니다"));

        if (license.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalArgumentException("승인 대기 상태의 면허증만 거절할 수 있습니다");
        }

        license.setApprovalStatus(ApprovalStatus.REJECTED);
        license.setApprovedBy(adminId);
        license.setApprovedAt(LocalDateTime.now());
        license.setRejectionReason(reason);
        licenseRepository.save(license);

        // 사용자 승인 상태 업데이트
        User user = license.getUser();
        user.setApprovalStatus(ApprovalStatus.REJECTED);
        userRepository.save(user);

        // 감사 로그 기록
        auditLogService.log(
                AuditActionType.REJECT,
                "LICENSE",
                licenseId,
                adminId,
                user.getId(),
                "면허증 거절: " + reason,
                request
        );

        return convertToResponse(license);
    }

    /**
     * 엔티티를 응답 DTO로 변환
     */
    private LicenseApprovalResponse convertToResponse(License license) {
        User user = license.getUser();
        return LicenseApprovalResponse.builder()
                .licenseId(license.getId())
                .userId(user.getId())
                .userName(user.getName())
                .hospitalName(user.getHospital() != null ? user.getHospital().getName() : null)
                .approvalStatus(license.getApprovalStatus())
                .rejectionReason(license.getRejectionReason())
                .approvedAt(license.getApprovedAt())
                .build();
    }
}

