package com.kabb.bloodbank.domain.entity;

import com.kabb.bloodbank.domain.enums.AgreementType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "agreements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Agreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgreementType agreementType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean agreed = true;

    @Column(length = 45)
    private String agreedIp; // 동의 시 IP

    @Column(length = 10)
    private String policyVersion; // 정책 버전 (예: "v1.0")

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime agreedAt; // 동의 시점

    @Column
    private LocalDateTime revokedAt; // 동의 철회 시점
}

