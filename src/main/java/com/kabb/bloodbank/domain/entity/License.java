package com.kabb.bloodbank.domain.entity;

import com.kabb.bloodbank.domain.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "licenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String fileName; // 원본 파일명

    @Column(nullable = false)
    private String storedFileName; // 저장된 파일명 (UUID 등)

    @Column(nullable = false)
    private String filePath; // 파일 저장 경로

    @Column(nullable = false)
    private Long fileSize; // 파일 크기 (bytes)

    @Column(nullable = false)
    private String contentType; // 파일 타입 (image/jpeg, application/pdf 등)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(length = 500)
    private String rejectionReason; // 거절 사유

    @Column
    private Long approvedBy; // 승인한 관리자 ID

    @Column
    private LocalDateTime approvedAt; // 승인 시점

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

