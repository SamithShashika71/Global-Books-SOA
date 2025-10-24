package com.globalbooks.shipping.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentEventDTO {
    private String eventType;
    private String paymentId;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime timestamp;
}