package com.globalbooks.payments.messaging;

import com.globalbooks.payments.config.RabbitMQConfig;
import com.globalbooks.payments.dto.PaymentEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentEvent(PaymentEventDTO event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.PAYMENT_STATUS_KEY,
                    event
            );
            log.info("Published payment event: {} for order: {}",
                    event.getEventType(), event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish payment event", e);
            // In production, implement retry logic or store for later processing
        }
    }

    public void publishPaymentResponse(String orderId, PaymentEventDTO response) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.PAYMENT_RESPONSE_KEY,
                    response
            );
            log.info("Published payment response for order: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to publish payment response for order: " + orderId, e);
        }
    }
}