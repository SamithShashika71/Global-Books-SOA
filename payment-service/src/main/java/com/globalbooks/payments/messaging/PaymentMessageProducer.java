package com.globalbooks.payments.messaging;

import com.globalbooks.payments.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendPaymentNotification(String targetQueue, Object message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    targetQueue,
                    message
            );
            log.info("Message sent to queue: {}", targetQueue);
        } catch (Exception e) {
            log.error("Failed to send message to queue: " + targetQueue, e);
            throw new RuntimeException("Failed to send message", e);
        }
    }
}