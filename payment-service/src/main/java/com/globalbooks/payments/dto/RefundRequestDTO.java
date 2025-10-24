package com.globalbooks.payments.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RefundRequestDTO {

    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @NotBlank(message = "Order ID is required")
    private String orderId;

    private String customerId;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid amount format")
    private BigDecimal refundAmount;

    private BigDecimal originalAmount;

    @NotBlank(message = "Refund reason is required")
    private String reason;

    private RefundType refundType;

    private String initiatedBy;

    private String approvedBy;

    private LocalDateTime requestedAt;

    private String metadata;

    public enum RefundType {
        FULL,
        PARTIAL,
        CREDIT
    }

    // Method to validate partial refund
    public boolean isValidPartialRefund() {
        if (refundType == RefundType.PARTIAL) {
            return refundAmount != null &&
                    originalAmount != null &&
                    refundAmount.compareTo(originalAmount) <= 0;
        }
        return true;
    }

    // Method to check if this is a full refund
    public boolean isFullRefund() {
        return refundType == RefundType.FULL ||
                (refundAmount != null &&
                        originalAmount != null &&
                        refundAmount.compareTo(originalAmount) == 0);
    }
}