package com.globalbooks.shipping.messaging;

import com.globalbooks.shipping.config.RabbitMQConfig;
import com.globalbooks.shipping.model.Shipment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShippingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishShipmentStatusUpdate(Shipment shipment) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", UUID.randomUUID().toString());
            event.put("eventType", "SHIPMENT_STATUS_UPDATED");
            event.put("shipmentId", shipment.getId());
            event.put("orderId", shipment.getOrderId());
            event.put("customerId", shipment.getCustomerId());
            event.put("status", shipment.getStatus().toString());
            event.put("trackingNumber", shipment.getTrackingNumber());
            event.put("carrier", shipment.getCarrier());
            event.put("estimatedDelivery", shipment.getEstimatedDelivery());
            event.put("timestamp", LocalDateTime.now());

            String routingKey = "shipping.status." + shipment.getStatus().toString().toLowerCase();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SHIPPING_EXCHANGE,
                    routingKey,
                    event
            );

            log.info("Published shipment status update: {} for order: {}",
                    shipment.getStatus(), shipment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish shipment status update", e);
        }
    }
}