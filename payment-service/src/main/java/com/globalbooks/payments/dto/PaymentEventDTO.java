package com.globalbooks.payments.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentEventDTO {
    private String eventId;
    private String eventType; // PAYMENT_INITIATED, PAYMENT_COMPLETED, PAYMENT_FAILED
    private String paymentId;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String status;
    private String message;
    private LocalDateTime timestamp;
}