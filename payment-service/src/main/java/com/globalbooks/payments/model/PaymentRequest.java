package com.globalbooks.payments.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Invalid currency code")
    private String currency = "USD";

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private PaymentDetails paymentDetails;

    @Data
    public static class PaymentDetails {
        private String cardNumber;
        private String cardHolderName;
        private String expiryMonth;
        private String expiryYear;
        private String cvv;
        private String email;
        private String billingAddress;
    }
}