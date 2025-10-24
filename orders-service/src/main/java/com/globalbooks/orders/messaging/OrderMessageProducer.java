package com.globalbooks.orders.messaging;

import com.globalbooks.orders.config.RabbitMQConfig;
import com.globalbooks.orders.dto.messaging.OrderEventMessage;
import com.globalbooks.orders.dto.messaging.PaymentRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Send payment request to PaymentsService
     */
    public void sendPaymentRequest(PaymentRequestMessage paymentRequest) {
        try {
            paymentRequest.setTimestamp(LocalDateTime.now());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.PAYMENT_REQUEST_KEY,
                    paymentRequest
            );

            log.info("Payment request sent for order: {}", paymentRequest.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send payment request for order: " + paymentRequest.getOrderId(), e);
            throw new RuntimeException("Failed to send payment request", e);
        }
    }

    /**
     * Publish order created event
     */
    public void publishOrderCreatedEvent(OrderEventMessage orderEvent) {
        try {
            orderEvent.setEventId(UUID.randomUUID().toString());
            orderEvent.setEventType("ORDER_CREATED");
            orderEvent.setTimestamp(LocalDateTime.now());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_CREATED_KEY,
                    orderEvent
            );

            log.info("Order created event published for order: {}", orderEvent.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish order created event", e);
        }
    }

    /**
     * Publish order status update
     */
    public void publishOrderStatusUpdate(String orderId, String status) {
        try {
            OrderEventMessage statusUpdate = OrderEventMessage.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("ORDER_STATUS_UPDATED")
                    .orderId(orderId)
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_STATUS_KEY,
                    statusUpdate
            );

            log.info("Order status update published: {} -> {}", orderId, status);
        } catch (Exception e) {
            log.error("Failed to publish order status update", e);
        }
    }
}