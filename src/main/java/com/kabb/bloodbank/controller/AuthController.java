package com.kabb.bloodbank.controller;

import com.kabb.bloodbank.dto.request.SignUpRequest;
import com.kabb.bloodbank.dto.response.ApiResponse;
import com.kabb.bloodbank.dto.response.SignUpResponse;
import com.kabb.bloodbank.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
            @Valid @ModelAttribute SignUpRequest request,
            HttpServletRequest httpRequest) {
        try {
            SignUpResponse response = userService.signUp(request, httpRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("회원가입이 완료되었습니다", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("회원가입 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}

