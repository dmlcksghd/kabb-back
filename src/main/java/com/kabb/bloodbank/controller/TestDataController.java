package com.kabb.bloodbank.controller;

import com.kabb.bloodbank.domain.entity.*;
import com.kabb.bloodbank.domain.enums.AgreementType;
import com.kabb.bloodbank.domain.enums.ApprovalStatus;
import com.kabb.bloodbank.domain.enums.UserRole;
import com.kabb.bloodbank.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 테스트용 더미 데이터 생성 컨트롤러
 * 운영 환경에서는 제거.
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestDataController {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final LicenseRepository licenseRepository;
    private final AgreementRepository agreementRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("")
    public ResponseEntity<Map<String, String>> testConnection() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "프론트와 백엔드 연결 성공!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("")
    public ResponseEntity<Map<String, Object>> testPostConnection(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("received", request);
        response.put("message", "프론트와 백엔드 POST 연결 성공");
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-test-user")
    public ResponseEntity<String> createTestUser() {
        // 테스트 사용자 생성
        User user = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .name("테스트 의사")
                .phone("010-1234-5678")
                .role(UserRole.USER)
                .approvalStatus(ApprovalStatus.PENDING)
                .registrationIp("127.0.0.1")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        // 병원 정보 생성
        Hospital hospital = Hospital.builder()
                .user(user)
                .name("테스트 병원")
                .address("서울시 강남구 테스트로 123")
                .phone("02-1234-5678")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        hospitalRepository.save(hospital);

        // 면허증 정보 생성 (더미 파일 경로)
        License license = License.builder()
                .user(user)
                .fileName("test-license.pdf")
                .storedFileName("test-uuid-12345.pdf")
                .filePath("/tmp/test-license.pdf")
                .fileSize(1024L)
                .contentType("application/pdf")
                .approvalStatus(ApprovalStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        licenseRepository.save(license);

        // 동의 기록 생성
        Agreement privacyAgreement = Agreement.builder()
                .user(user)
                .agreementType(AgreementType.PRIVACY_POLICY)
                .agreed(true)
                .agreedIp("127.0.0.1")
                .policyVersion("v1.0")
                .agreedAt(LocalDateTime.now())
                .build();
        agreementRepository.save(privacyAgreement);

        Agreement termsAgreement = Agreement.builder()
                .user(user)
                .agreementType(AgreementType.TERMS_OF_SERVICE)
                .agreed(true)
                .agreedIp("127.0.0.1")
                .policyVersion("v1.0")
                .agreedAt(LocalDateTime.now())
                .build();
        agreementRepository.save(termsAgreement);

        Agreement sensitiveAgreement = Agreement.builder()
                .user(user)
                .agreementType(AgreementType.SENSITIVE_INFO)
                .agreed(true)
                .agreedIp("127.0.0.1")
                .policyVersion("v1.0")
                .agreedAt(LocalDateTime.now())
                .build();
        agreementRepository.save(sensitiveAgreement);

        return ResponseEntity.ok("테스트 사용자 생성 완료!\n" +
                "User ID: " + user.getId() + "\n" +
                "License ID: " + license.getId() + "\n" +
                "이제 승인 대기 목록을 조회하거나 승인/거절을 테스트할 수 있습니다.");
    }
}

