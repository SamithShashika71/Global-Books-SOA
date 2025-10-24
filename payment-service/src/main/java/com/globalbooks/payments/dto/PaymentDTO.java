package com.globalbooks.payments.dto;

import com.globalbooks.payments.model.PaymentMethod;
import com.globalbooks.payments.model.PaymentStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private String id;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}