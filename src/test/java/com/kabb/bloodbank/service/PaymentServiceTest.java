package com.kabb.bloodbank.service;

import com.kabb.bloodbank.domain.entity.Order;
import com.kabb.bloodbank.domain.entity.Payment;
import com.kabb.bloodbank.domain.entity.User;
import com.kabb.bloodbank.domain.enums.OrderStatus;
import com.kabb.bloodbank.domain.enums.PaymentStatus;
import com.kabb.bloodbank.dto.request.PaymentConfirmRequest;
import com.kabb.bloodbank.dto.response.PaymentResponse;
import com.kabb.bloodbank.dto.response.TossPaymentResponse;
import com.kabb.bloodbank.repository.OrderRepository;
import com.kabb.bloodbank.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private TossPaymentService tossPaymentService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void confirmPaymentFailsWhenOrderMissing() {
        when(orderRepository.findByOrderNumber("ORD-1")).thenReturn(Optional.empty());

        PaymentConfirmRequest request = PaymentConfirmRequest.builder()
                .orderId("ORD-1")
                .paymentKey("pay-1")
                .amount(new BigDecimal("100.00"))
                .build();

        assertThrows(RuntimeException.class, () -> paymentService.confirmPayment(request, 1L));
    }

    @Test
    void confirmPaymentFailsWhenUserMismatch() {
        User owner = User.builder().id(1L).build();
        Order order = Order.builder()
                .id(10L)
                .orderNumber("ORD-1")
                .user(owner)
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findByOrderNumber("ORD-1")).thenReturn(Optional.of(order));

        PaymentConfirmRequest request = PaymentConfirmRequest.builder()
                .orderId("ORD-1")
                .paymentKey("pay-1")
                .amount(new BigDecimal("100.00"))
                .build();

        assertThrows(RuntimeException.class, () -> paymentService.confirmPayment(request, 2L));
        verifyNoInteractions(tossPaymentService);
    }

    @Test
    void confirmPaymentFailsWhenAmountMismatch() {
        User owner = User.builder().id(1L).build();
        Order order = Order.builder()
                .id(10L)
                .orderNumber("ORD-1")
                .user(owner)
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findByOrderNumber("ORD-1")).thenReturn(Optional.of(order));

        PaymentConfirmRequest request = PaymentConfirmRequest.builder()
                .orderId("ORD-1")
                .paymentKey("pay-1")
                .amount(new BigDecimal("90.00"))
                .build();

        assertThrows(RuntimeException.class, () -> paymentService.confirmPayment(request, 1L));
        verifyNoInteractions(tossPaymentService);
    }

    @Test
    void confirmPaymentCompletesWhenTossDone() {
        User owner = User.builder().id(1L).build();
        Order order = Order.builder()
                .id(10L)
                .orderNumber("ORD-1")
                .user(owner)
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .build();

        TossPaymentResponse tossResponse = new TossPaymentResponse();
        tossResponse.setStatus("DONE");
        tossResponse.setMethod("CARD");
        tossResponse.setPaymentKey("pay-1");

        when(orderRepository.findByOrderNumber("ORD-1")).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(tossPaymentService.confirmPayment(any())).thenReturn(tossResponse);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentConfirmRequest request = PaymentConfirmRequest.builder()
                .orderId("ORD-1")
                .paymentKey("pay-1")
                .amount(new BigDecimal("100.00"))
                .build();

        PaymentResponse response = paymentService.confirmPayment(request, 1L);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();

        assertEquals(PaymentStatus.COMPLETED, savedPayment.getStatus());
        assertEquals("CARD", savedPayment.getPaymentMethod());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertNotNull(response.getPaymentNumber());
    }

    @Test
    void confirmPaymentMarksFailedWhenTossNotDone() {
        User owner = User.builder().id(1L).build();
        Order order = Order.builder()
                .id(10L)
                .orderNumber("ORD-1")
                .user(owner)
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .build();

        TossPaymentResponse tossResponse = new TossPaymentResponse();
        tossResponse.setStatus("FAILED");
        tossResponse.setMethod("CARD");
        tossResponse.setPaymentKey("pay-1");

        when(orderRepository.findByOrderNumber("ORD-1")).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(tossPaymentService.confirmPayment(any())).thenReturn(tossResponse);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentConfirmRequest request = PaymentConfirmRequest.builder()
                .orderId("ORD-1")
                .paymentKey("pay-1")
                .amount(new BigDecimal("100.00"))
                .build();

        PaymentResponse response = paymentService.confirmPayment(request, 1L);

        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }
}
