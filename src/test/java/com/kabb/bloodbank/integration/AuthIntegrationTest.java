package com.kabb.bloodbank.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kabb.bloodbank.domain.entity.User;
import com.kabb.bloodbank.domain.enums.ApprovalStatus;
import com.kabb.bloodbank.domain.enums.UserRole;
import com.kabb.bloodbank.dto.request.LoginRequest;
import com.kabb.bloodbank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void loginAndFetchCurrentUser() throws Exception {
        createUser("user@example.com", "password123", UserRole.USER);

        String token = loginAndGetToken("user@example.com", "password123");

        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void userTokenCannotAccessAdminEndpoints() throws Exception {
        createUser("user@example.com", "password123", UserRole.USER);

        String token = loginAndGetToken("user@example.com", "password123");

        mockMvc.perform(get("/api/admin/licenses/pending")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminTokenCanAccessAdminEndpoints() throws Exception {
        createUser("admin@example.com", "password123", UserRole.ADMIN);

        String token = loginAndGetToken("admin@example.com", "password123");

        mockMvc.perform(get("/api/admin/licenses/pending")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private void createUser(String email, String rawPassword, UserRole role) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .name("Test User")
                .phone("010-1234-5678")
                .role(role)
                .approvalStatus(ApprovalStatus.APPROVED)
                .active(true)
                .build();
        userRepository.save(user);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(responseBody);
        return root.path("data").path("accessToken").asText();
    }
}
