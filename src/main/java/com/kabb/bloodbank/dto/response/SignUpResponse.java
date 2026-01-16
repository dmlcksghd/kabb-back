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
public class SignUpResponse {
    private Long userId;
    private String email;
    private String name;
    private ApprovalStatus approvalStatus;
    private String message;
    private LocalDateTime createdAt;
}

