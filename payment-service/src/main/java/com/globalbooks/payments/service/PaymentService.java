package com.globalbooks.payments.service;

import com.globalbooks.payments.model.PaymentRequest;
import com.globalbooks.payments.model.PaymentResponse;
import com.globalbooks.payments.dto.PaymentDTO;
import java.util.List;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);
    PaymentDTO getPaymentById(String paymentId);
    PaymentDTO getPaymentByOrderId(String orderId);
    List<PaymentDTO> getPaymentsByCustomer(String customerId);
    PaymentResponse refundPayment(String paymentId, String reason);
    void cancelPayment(String paymentId);
    PaymentResponse retryPayment(String paymentId);
}