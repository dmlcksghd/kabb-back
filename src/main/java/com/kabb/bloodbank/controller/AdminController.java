package com.kabb.bloodbank.controller;

import com.kabb.bloodbank.dto.response.ApiResponse;
import com.kabb.bloodbank.dto.response.LicenseApprovalResponse;
import com.kabb.bloodbank.service.LicenseApprovalService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final LicenseApprovalService licenseApprovalService;

    /**
     * 승인 대기 면허증 목록 조회
     */
    @GetMapping("/licenses/pending")
    public ResponseEntity<ApiResponse<List<LicenseApprovalResponse>>> getPendingLicenses() {
        List<LicenseApprovalResponse> licenses = licenseApprovalService.getPendingLicenses();
        return ResponseEntity.ok(ApiResponse.success(licenses));
    }

    /**
     * 면허증 승인
     */
    @PostMapping("/licenses/{licenseId}/approve")
    public ResponseEntity<ApiResponse<LicenseApprovalResponse>> approveLicense(
            @PathVariable Long licenseId,
            @RequestParam Long adminId,
            HttpServletRequest request) {
        try {
            LicenseApprovalResponse response = licenseApprovalService.approveLicense(licenseId, adminId, request);
            return ResponseEntity.ok(ApiResponse.success("면허증이 승인되었습니다", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("승인 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 면허증 거절
     */
    @PostMapping("/licenses/{licenseId}/reject")
    public ResponseEntity<ApiResponse<LicenseApprovalResponse>> rejectLicense(
            @PathVariable Long licenseId,
            @RequestParam Long adminId,
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            String reason = requestBody.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("거절 사유를 입력해주세요"));
            }

            LicenseApprovalResponse response = licenseApprovalService.rejectLicense(licenseId, adminId, reason, request);
            return ResponseEntity.ok(ApiResponse.success("면허증이 거절되었습니다", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("거절 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}

