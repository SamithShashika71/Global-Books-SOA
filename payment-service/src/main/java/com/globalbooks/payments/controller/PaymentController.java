package com.globalbooks.payments.controller;

import com.globalbooks.payments.dto.PaymentDTO;
import com.globalbooks.payments.model.PaymentRequest;
import com.globalbooks.payments.model.PaymentResponse;
import com.globalbooks.payments.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for payment processing")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Process a new payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<PaymentDTO> getPayment(
            @Parameter(description = "Payment ID") @PathVariable String paymentId) {
        PaymentDTO payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID")
    public ResponseEntity<PaymentDTO> getPaymentByOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        PaymentDTO payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all payments for a customer")
    public ResponseEntity<List<PaymentDTO>> getCustomerPayments(
            @Parameter(description = "Customer ID") @PathVariable String customerId) {
        List<PaymentDTO> payments = paymentService.getPaymentsByCustomer(customerId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund a payment")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable String paymentId,
            @RequestBody Map<String, String> request) {
        String reason = request.getOrDefault("reason", "Customer request");
        PaymentResponse response = paymentService.refundPayment(paymentId, reason);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentId}/cancel")
    @Operation(summary = "Cancel a pending payment")
    public ResponseEntity<Void> cancelPayment(@PathVariable String paymentId) {
        paymentService.cancelPayment(paymentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{paymentId}/retry")
    @Operation(summary = "Retry a failed payment")
    public ResponseEntity<PaymentResponse> retryPayment(@PathVariable String paymentId) {
        PaymentResponse response = paymentService.retryPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "payments-service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}