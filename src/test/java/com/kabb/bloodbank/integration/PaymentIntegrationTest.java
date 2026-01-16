package com.kabb.bloodbank.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kabb.bloodbank.domain.entity.Order;
import com.kabb.bloodbank.domain.entity.User;
import com.kabb.bloodbank.domain.enums.ApprovalStatus;
import com.kabb.bloodbank.domain.enums.OrderStatus;
import com.kabb.bloodbank.domain.enums.UserRole;
import com.kabb.bloodbank.dto.request.LoginRequest;
import com.kabb.bloodbank.dto.request.PaymentConfirmRequest;
import com.kabb.bloodbank.dto.response.TossPaymentResponse;
import com.kabb.bloodbank.repository.OrderRepository;
import com.kabb.bloodbank.repository.PaymentRepository;
import com.kabb.bloodbank.repository.UserRepository;
import com.kabb.bloodbank.service.TossPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private TossPaymentService tossPaymentService;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void confirmPaymentCompletesOrderWhenTossDone() throws Exception {
        User user = createUser("user@example.com", "password123");
        Order order = createOrder(user, "ORD-2025-001", new BigDecimal("100.00"));

        TossPaymentResponse tossResponse = new TossPaymentResponse();
        tossResponse.setStatus("DONE");
        tossResponse.setMethod("CARD");
        tossResponse.setPaymentKey("pay-1");
        when(tossPaymentService.confirmPayment(any())).thenReturn(tossResponse);

        String token = loginAndGetToken("user@example.com", "password123");

        PaymentConfirmRequest request = PaymentConfirmRequest.builder()
                .orderId(order.getOrderNumber())
                .paymentKey("pay-1")
                .amount(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/payments/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, updatedOrder.getStatus());
    }

    private User createUser(String email, String rawPassword) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .name("Test User")
                .phone("010-1234-5678")
                .role(UserRole.USER)
                .approvalStatus(ApprovalStatus.APPROVED)
                .active(true)
                .build();
        return userRepository.save(user);
    }

    private Order createOrder(User user, String orderNumber, BigDecimal amount) {
        Order order = Order.builder()
                .user(user)
                .orderNumber(orderNumber)
                .totalAmount(amount)
                .status(OrderStatus.PENDING)
                .build();
        return orderRepository.save(order);
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

        return objectMapper.readTree(responseBody)
                .path("data")
                .path("accessToken")
                .asText();
    }
}
