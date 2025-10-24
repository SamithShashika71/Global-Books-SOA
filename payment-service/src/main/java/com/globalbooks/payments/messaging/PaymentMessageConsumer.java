package com.globalbooks.payments.messaging;

import com.globalbooks.payments.config.RabbitMQConfig;
import com.globalbooks.payments.dto.PaymentEventDTO;
import com.globalbooks.payments.model.PaymentRequest;
import com.globalbooks.payments.model.PaymentResponse;
import com.globalbooks.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentMessageConsumer {

    private final PaymentService paymentService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_REQUEST_QUEUE)
    public void handlePaymentRequest(PaymentRequest request) {
        log.info("Received payment request for order: {}", request.getOrderId());

        try {
            // Process the payment
            PaymentResponse response = paymentService.processPayment(request);

            // Send response to response queue
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.PAYMENT_RESPONSE_KEY,
                    response
            );

            log.info("Payment processed successfully for order: {}", request.getOrderId());

        } catch (Exception e) {
            log.error("Error processing payment for order: " + request.getOrderId(), e);

            // Send failure response
            PaymentResponse failureResponse = PaymentResponse.builder()
                    .orderId(request.getOrderId())
                    .customerId(request.getCustomerId())
                    .amount(request.getAmount())
                    .status(com.globalbooks.payments.model.PaymentStatus.FAILED)
                    .message("Payment processing failed: " + e.getMessage())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.PAYMENT_RESPONSE_KEY,
                    failureResponse
            );

            // Message will be sent to DLQ if it fails multiple times
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_DLQ)
    public void handleDeadLetterMessage(Object message) {
        log.error("Received dead letter message: {}", message);
    }
}