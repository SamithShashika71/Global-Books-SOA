package com.globalbooks.payments.model;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private String paymentId;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String transactionId;
    private String message;
    private LocalDateTime processedAt;
}