package com.kabb.bloodbank.service;

import com.kabb.bloodbank.domain.entity.*;
import com.kabb.bloodbank.domain.enums.AgreementType;
import com.kabb.bloodbank.domain.enums.ApprovalStatus;
import com.kabb.bloodbank.domain.enums.AuditActionType;
import com.kabb.bloodbank.domain.enums.UserRole;
import com.kabb.bloodbank.dto.request.LoginRequest;
import com.kabb.bloodbank.dto.request.SignUpRequest;
import com.kabb.bloodbank.dto.response.LoginResponse;
import com.kabb.bloodbank.dto.response.SignUpResponse;
import com.kabb.bloodbank.util.JwtUtil;
import com.kabb.bloodbank.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final LicenseRepository licenseRepository;
    private final AgreementRepository agreementRepository;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입 처리
     */
    @Transactional
    public SignUpResponse signUp(SignUpRequest request, HttpServletRequest httpRequest) throws Exception {
        // 1. 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다");
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 면허증 파일 저장
        FileStorageService.FileStorageResult fileResult = fileStorageService.storeLicenseFile(request.getLicenseFile());

        // 4. 사용자 생성
        String clientIp = getClientIpAddress(httpRequest);
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .phone(request.getPhone())
                .role(UserRole.USER)
                .approvalStatus(ApprovalStatus.PENDING)
                .registrationIp(clientIp)
                .active(true)
                .build();

        user = userRepository.save(user);

        // 5. 병원 정보 저장
        Hospital hospital = Hospital.builder()
                .user(user)
                .name(request.getHospitalName())
                .address(request.getHospitalAddress())
                .phone(request.getHospitalPhone())
                .businessNumber(request.getBusinessNumber())
                .build();

        hospitalRepository.save(hospital);

        // 6. 면허증 정보 저장
        License license = License.builder()
                .user(user)
                .fileName(fileResult.getOriginalFileName())
                .storedFileName(fileResult.getStoredFileName())
                .filePath(fileResult.getFilePath())
                .fileSize(fileResult.getFileSize())
                .contentType(fileResult.getContentType())
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        licenseRepository.save(license);

        // 7. 동의 기록 저장
        if (Boolean.TRUE.equals(request.getPrivacyPolicyAgreed())) {
            Agreement privacyAgreement = Agreement.builder()
                    .user(user)
                    .agreementType(AgreementType.PRIVACY_POLICY)
                    .agreed(true)
                    .agreedIp(clientIp)
                    .policyVersion("v1.0")
                    .build();
            agreementRepository.save(privacyAgreement);
        }

        if (Boolean.TRUE.equals(request.getTermsOfServiceAgreed())) {
            Agreement termsAgreement = Agreement.builder()
                    .user(user)
                    .agreementType(AgreementType.TERMS_OF_SERVICE)
                    .agreed(true)
                    .agreedIp(clientIp)
                    .policyVersion("v1.0")
                    .build();
            agreementRepository.save(termsAgreement);
        }

        if (Boolean.TRUE.equals(request.getSensitiveInfoAgreed())) {
            Agreement sensitiveAgreement = Agreement.builder()
                    .user(user)
                    .agreementType(AgreementType.SENSITIVE_INFO)
                    .agreed(true)
                    .agreedIp(clientIp)
                    .policyVersion("v1.0")
                    .build();
            agreementRepository.save(sensitiveAgreement);
        }

        // 8. 감사 로그 기록
        auditLogService.log(
                AuditActionType.CREATE,
                "USER",
                user.getId(),
                user.getId(),
                null,
                "회원가입 완료",
                httpRequest
        );

        // 9. 응답 생성
        return SignUpResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .approvalStatus(user.getApprovalStatus())
                .message("회원가입이 완료되었습니다. 관리자 승인 후 서비스를 이용하실 수 있습니다.")
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 로그인 처리
     */
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // 1. 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다"));

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        // 3. 활성화 여부 확인
        if (!user.getActive()) {
            throw new IllegalArgumentException("비활성화된 계정입니다");
        }

        // 4. JWT 토큰 생성
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        // 5. 감사 로그 기록
        auditLogService.log(
                AuditActionType.LOGIN,
                "USER",
                user.getId(),
                user.getId(),
                null,
                "로그인 성공",
                httpRequest
        );

        // 6. 응답 생성
        return LoginResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .approvalStatus(user.getApprovalStatus())
                .build();
    }
}

