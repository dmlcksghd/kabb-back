package com.kabb.bloodbank.dto.response;

import com.kabb.bloodbank.domain.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseApprovalResponse {
    private Long licenseId;
    private Long userId;
    private String userName;
    private String hospitalName;
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
    private LocalDateTime approvedAt;
}

