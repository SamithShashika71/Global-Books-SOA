package com.globalbooks.orders.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestMessage {
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private PaymentDetails paymentDetails;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDetails {
        private String cardNumber;
        private String cardHolderName;
        private String expiryMonth;
        private String expiryYear;
        private String cvv;
        private String email;
    }
}