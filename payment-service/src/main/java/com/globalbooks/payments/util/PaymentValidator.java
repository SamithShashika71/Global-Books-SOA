package com.globalbooks.payments.util;

import com.globalbooks.payments.exception.PaymentException;
import com.globalbooks.payments.model.PaymentMethod;
import com.globalbooks.payments.model.PaymentRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class PaymentValidator {

    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{13,19}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^[0-9]{3,4}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("999999.99");
    private static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("0.01");

    // Supported currencies
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR", "GBP", "CAD", "AUD");

    /**
     * Validates the complete payment request
     */
    public void validatePaymentRequest(PaymentRequest request) {
        if (request == null) {
            throw new PaymentException("Payment request cannot be null");
        }

        validateOrderId(request.getOrderId());
        validateCustomerId(request.getCustomerId());
        validateAmount(request.getAmount());
        validateCurrency(request.getCurrency());
        validatePaymentMethod(request.getPaymentMethod());

        // Validate payment details based on payment method
        if (request.getPaymentMethod() == PaymentMethod.CREDIT_CARD ||
                request.getPaymentMethod() == PaymentMethod.DEBIT_CARD) {
            validateCardPaymentDetails(request.getPaymentDetails());
        }
    }

    /**
     * Validates order ID format
     */
    public void validateOrderId(String orderId) {
        if (StringUtils.isBlank(orderId)) {
            throw new PaymentException("Order ID cannot be empty");
        }
        if (orderId.length() > 50) {
            throw new PaymentException("Order ID is too long");
        }
    }

    /**
     * Validates customer ID format
     */
    public void validateCustomerId(String customerId) {
        if (StringUtils.isBlank(customerId)) {
            throw new PaymentException("Customer ID cannot be empty");
        }
        if (customerId.length() > 50) {
            throw new PaymentException("Customer ID is too long");
        }
    }

    /**
     * Validates payment amount
     */
    public void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new PaymentException("Amount cannot be null");
        }
        if (amount.compareTo(MIN_TRANSACTION_AMOUNT) < 0) {
            throw new PaymentException("Amount must be at least " + MIN_TRANSACTION_AMOUNT);
        }
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new PaymentException("Amount exceeds maximum limit of " + MAX_TRANSACTION_AMOUNT);
        }
        if (amount.scale() > 2) {
            throw new PaymentException("Amount can have maximum 2 decimal places");
        }
    }

    /**
     * Validates currency code
     */
    public void validateCurrency(String currency) {
        if (StringUtils.isBlank(currency)) {
            throw new PaymentException("Currency cannot be empty");
        }

        // Check if it's a valid ISO currency code
        try {
            Currency.getInstance(currency);
        } catch (IllegalArgumentException e) {
            throw new PaymentException("Invalid currency code: " + currency);
        }

        // Check if we support this currency
        if (!SUPPORTED_CURRENCIES.contains(currency.toUpperCase())) {
            throw new PaymentException("Currency not supported: " + currency);
        }
    }

    /**
     * Validates payment method
     */
    public void validatePaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new PaymentException("Payment method cannot be null");
        }
    }

    /**
     * Validates card payment details
     */
    public void validateCardPaymentDetails(PaymentRequest.PaymentDetails details) {
        if (details == null) {
            throw new PaymentException("Card details are required for card payments");
        }

        validateCardNumber(details.getCardNumber());
        validateCardHolderName(details.getCardHolderName());
        validateCardExpiry(details.getExpiryMonth(), details.getExpiryYear());
        validateCVV(details.getCvv());

        if (StringUtils.isNotBlank(details.getEmail())) {
            validateEmail(details.getEmail());
        }
    }

    /**
     * Validates card number using Luhn algorithm
     */
    public boolean validateCardNumber(String cardNumber) {
        if (StringUtils.isBlank(cardNumber)) {
            throw new PaymentException("Card number is required");
        }

        // Remove spaces and dashes
        String cleanCardNumber = cardNumber.replaceAll("[\\s-]", "");

        if (!CARD_NUMBER_PATTERN.matcher(cleanCardNumber).matches()) {
            throw new PaymentException("Invalid card number format");
        }

        // Luhn algorithm validation
        if (!isValidLuhn(cleanCardNumber)) {
            throw new PaymentException("Invalid card number");
        }

        return true;
    }

    /**
     * Luhn algorithm implementation
     */
    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    /**
     * Validates card holder name
     */
    public void validateCardHolderName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new PaymentException("Card holder name is required");
        }
        if (name.length() < 2) {
            throw new PaymentException("Card holder name is too short");
        }
        if (name.length() > 100) {
            throw new PaymentException("Card holder name is too long");
        }
        if (!name.matches("^[a-zA-Z\\s'-]+$")) {
            throw new PaymentException("Card holder name contains invalid characters");
        }
    }

    /**
     * Validates card expiry date
     */
    public void validateCardExpiry(String month, String year) {
        if (StringUtils.isBlank(month) || StringUtils.isBlank(year)) {
            throw new PaymentException("Card expiry date is required");
        }

        try {
            int expMonth = Integer.parseInt(month);
            int expYear = Integer.parseInt(year);

            if (expMonth < 1 || expMonth > 12) {
                throw new PaymentException("Invalid expiry month");
            }

            // Convert 2-digit year to 4-digit
            if (expYear < 100) {
                expYear += 2000;
            }

            LocalDate now = LocalDate.now();
            LocalDate expiry = LocalDate.of(expYear, expMonth, 1).plusMonths(1).minusDays(1);

            if (expiry.isBefore(now)) {
                throw new PaymentException("Card has expired");
            }

        } catch (NumberFormatException e) {
            throw new PaymentException("Invalid expiry date format");
        }
    }

    /**
     * Validates CVV
     */
    public void validateCVV(String cvv) {
        if (StringUtils.isBlank(cvv)) {
            throw new PaymentException("CVV is required");
        }
        if (!CVV_PATTERN.matcher(cvv).matches()) {
            throw new PaymentException("Invalid CVV format");
        }
    }

    /**
     * Validates email format
     */
    public void validateEmail(String email) {
        if (StringUtils.isNotBlank(email) && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new PaymentException("Invalid email format");
        }
    }

    /**
     * Determines card type from card number
     */
    public String getCardType(String cardNumber) {
        String cleanCardNumber = cardNumber.replaceAll("[\\s-]", "");

        if (cleanCardNumber.startsWith("4")) {
            return "VISA";
        } else if (cleanCardNumber.startsWith("5") &&
                Integer.parseInt(cleanCardNumber.substring(0, 2)) >= 51 &&
                Integer.parseInt(cleanCardNumber.substring(0, 2)) <= 55) {
            return "MASTERCARD";
        } else if (cleanCardNumber.startsWith("34") || cleanCardNumber.startsWith("37")) {
            return "AMEX";
        } else if (cleanCardNumber.startsWith("6011") || cleanCardNumber.startsWith("65")) {
            return "DISCOVER";
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * Masks card number for display
     */
    public String maskCardNumber(String cardNumber) {
        if (StringUtils.isBlank(cardNumber)) {
            return "";
        }

        String cleanCardNumber = cardNumber.replaceAll("[\\s-]", "");
        if (cleanCardNumber.length() <= 4) {
            return cleanCardNumber;
        }

        String lastFour = cleanCardNumber.substring(cleanCardNumber.length() - 4);
        String masked = StringUtils.repeat("*", cleanCardNumber.length() - 4) + lastFour;

        // Format as XXXX XXXX XXXX 1234
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < masked.length(); i += 4) {
            if (i > 0) {
                formatted.append(" ");
            }
            formatted.append(masked.substring(i, Math.min(i + 4, masked.length())));
        }

        return formatted.toString();
    }

    /**
     * Validates refund amount against original payment
     */
    public void validateRefundAmount(BigDecimal refundAmount, BigDecimal originalAmount) {
        if (refundAmount == null || originalAmount == null) {
            throw new PaymentException("Refund amount and original amount are required");
        }

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Refund amount must be positive");
        }

        if (refundAmount.compareTo(originalAmount) > 0) {
            throw new PaymentException("Refund amount cannot exceed original payment amount");
        }
    }
}