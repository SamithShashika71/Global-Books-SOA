package com.globalbooks.payments.service.impl;

import com.globalbooks.payments.dto.PaymentDTO;
import com.globalbooks.payments.dto.PaymentEventDTO;
import com.globalbooks.payments.exception.PaymentException;
import com.globalbooks.payments.messaging.PaymentEventPublisher;
import com.globalbooks.payments.model.*;
import com.globalbooks.payments.repository.PaymentRepository;
import com.globalbooks.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());

        // Check if payment already exists for this order
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new PaymentException("Payment already exists for order: " + request.getOrderId());
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setCustomerId(request.getCustomerId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);

        payment = paymentRepository.save(payment);

        // Publish payment initiated event
        eventPublisher.publishPaymentEvent(PaymentEventDTO.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PAYMENT_INITIATED")
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status("PENDING")
                .timestamp(LocalDateTime.now())
                .build());

        // Simulate payment processing
        try {
            payment.setStatus(PaymentStatus.PROCESSING);
            paymentRepository.save(payment);

            // Simulate payment gateway call
            boolean paymentSuccess = processWithPaymentGateway(request);

            if (paymentSuccess) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setTransactionId(generateTransactionId());
                payment.setProcessedAt(LocalDateTime.now());

                // Publish payment completed event
                eventPublisher.publishPaymentEvent(PaymentEventDTO.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType("PAYMENT_COMPLETED")
                        .paymentId(payment.getId())
                        .orderId(payment.getOrderId())
                        .customerId(payment.getCustomerId())
                        .amount(payment.getAmount())
                        .status("COMPLETED")
                        .message("Payment processed successfully")
                        .timestamp(LocalDateTime.now())
                        .build());
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Payment declined by gateway");

                // Publish payment failed event
                eventPublisher.publishPaymentEvent(PaymentEventDTO.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType("PAYMENT_FAILED")
                        .paymentId(payment.getId())
                        .orderId(payment.getOrderId())
                        .customerId(payment.getCustomerId())
                        .amount(payment.getAmount())
                        .status("FAILED")
                        .message("Payment failed")
                        .timestamp(LocalDateTime.now())
                        .build());
            }

        } catch (Exception e) {
            log.error("Error processing payment: ", e);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
        }

        payment = paymentRepository.save(payment);

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .message(payment.getStatus() == PaymentStatus.COMPLETED ?
                        "Payment successful" : "Payment failed: " + payment.getFailureReason())
                .processedAt(payment.getProcessedAt())
                .build();
    }

    @Override
    public PaymentDTO getPaymentById(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));
        return convertToDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException("Payment not found for order: " + orderId));
        return convertToDTO(payment);
    }

    @Override
    public List<PaymentDTO> getPaymentsByCustomer(String customerId) {
        return paymentRepository.findByCustomerId(customerId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(String paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentException("Can only refund completed payments");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // Publish refund event
        eventPublisher.publishPaymentEvent(PaymentEventDTO.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PAYMENT_REFUNDED")
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status("REFUNDED")
                .message("Payment refunded: " + reason)
                .timestamp(LocalDateTime.now())
                .build());

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .message("Refund processed successfully")
                .processedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public void cancelPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentException("Can only cancel pending payments");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Publish cancellation event
        eventPublisher.publishPaymentEvent(PaymentEventDTO.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PAYMENT_CANCELLED")
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status("CANCELLED")
                .message("Payment cancelled")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional
    public PaymentResponse retryPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new PaymentException("Can only retry failed payments");
        }

        // Create new payment request from existing payment
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(payment.getOrderId());
        request.setCustomerId(payment.getCustomerId());
        request.setAmount(payment.getAmount());
        request.setCurrency(payment.getCurrency());
        request.setPaymentMethod(payment.getPaymentMethod());

        // Mark old payment as cancelled
        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        // Process new payment
        return processPayment(request);
    }

    private boolean processWithPaymentGateway(PaymentRequest request) {
        // Simulate payment gateway processing
        // In production, this would call actual payment gateway API
        try {
            Thread.sleep(1000); // Simulate processing time

            // Simulate 90% success rate
            return Math.random() > 0.1;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrderId());
        dto.setCustomerId(payment.getCustomerId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setProcessedAt(payment.getProcessedAt());
        return dto;
    }
}