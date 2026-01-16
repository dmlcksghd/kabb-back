package com.kabb.bloodbank.service;

import com.kabb.bloodbank.domain.entity.User;
import com.kabb.bloodbank.domain.enums.ApprovalStatus;
import com.kabb.bloodbank.domain.enums.UserRole;
import com.kabb.bloodbank.dto.request.LoginRequest;
import com.kabb.bloodbank.dto.request.SignUpRequest;
import com.kabb.bloodbank.dto.response.LoginResponse;
import com.kabb.bloodbank.repository.AgreementRepository;
import com.kabb.bloodbank.repository.HospitalRepository;
import com.kabb.bloodbank.repository.LicenseRepository;
import com.kabb.bloodbank.repository.UserRepository;
import com.kabb.bloodbank.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private HospitalRepository hospitalRepository;
    @Mock
    private LicenseRepository licenseRepository;
    @Mock
    private AgreementRepository agreementRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @Test
    void loginSuccess() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded")
                .name("Test User")
                .role(UserRole.USER)
                .approvalStatus(ApprovalStatus.APPROVED)
                .active(true)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "test@example.com", "USER")).thenReturn("token");

        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        LoginResponse response = userService.login(request, null);

        assertEquals("token", response.getAccessToken());
        assertEquals(1L, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
        assertEquals("USER", response.getRole());
        assertEquals(ApprovalStatus.APPROVED, response.getApprovalStatus());
        verify(auditLogService, times(1)).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void loginFailsWithWrongPassword() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded")
                .role(UserRole.USER)
                .approvalStatus(ApprovalStatus.APPROVED)
                .active(true)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrong")
                .build();

        assertThrows(IllegalArgumentException.class, () -> userService.login(request, null));
        verify(auditLogService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void loginFailsWhenUserInactive() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded")
                .role(UserRole.USER)
                .approvalStatus(ApprovalStatus.APPROVED)
                .active(false)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);

        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        assertThrows(IllegalArgumentException.class, () -> userService.login(request, null));
        verify(auditLogService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void signUpRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        SignUpRequest request = SignUpRequest.builder()
                .email("duplicate@example.com")
                .build();

        assertThrows(IllegalArgumentException.class, () -> userService.signUp(request, null));
        verifyNoInteractions(fileStorageService);
    }
}
