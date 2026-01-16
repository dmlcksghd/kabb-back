package com.kabb.bloodbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "휴대폰 번호는 필수입니다")
    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다")
    private String phone;

    // 병원 정보
    @NotBlank(message = "병원명은 필수입니다")
    private String hospitalName;

    @NotBlank(message = "병원 주소는 필수입니다")
    private String hospitalAddress;

    @NotBlank(message = "병원 전화번호는 필수입니다")
    private String hospitalPhone;

    private String businessNumber; // 사업자등록번호 (선택)

    // 면허증 파일
    @NotNull(message = "면허증 파일은 필수입니다")
    private MultipartFile licenseFile;

    // 동의 항목
    @NotNull(message = "개인정보처리방침 동의는 필수입니다")
    private Boolean privacyPolicyAgreed;

    @NotNull(message = "서비스 이용약관 동의는 필수입니다")
    private Boolean termsOfServiceAgreed;

    @NotNull(message = "민감정보 처리 동의는 필수입니다")
    private Boolean sensitiveInfoAgreed;
}

