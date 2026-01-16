package com.kabb.bloodbank.domain.entity;

import com.kabb.bloodbank.domain.enums.AuditActionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userId; // 작업 수행한 사용자 ID

    @Column
    private Long targetUserId; // 대상 사용자 ID (개인정보 조회 시 등)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditActionType actionType;

    @Column(nullable = false)
    private String resourceType; // 리소스 타입 (USER, LICENSE, AGREEMENT 등)

    @Column
    private Long resourceId; // 리소스 ID

    @Column(length = 1000)
    private String description; // 작업 설명

    @Column(length = 45)
    private String ipAddress; // 접근 IP

    @Column(length = 500)
    private String userAgent; // 사용자 에이전트

    @Column(columnDefinition = "TEXT")
    private String requestData; // 요청 데이터 (JSON)

    @Column(columnDefinition = "TEXT")
    private String responseData; // 응답 데이터 (JSON)

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

