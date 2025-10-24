package com.globalbooks.shipping.messaging;

import com.globalbooks.shipping.config.RabbitMQConfig;
import com.globalbooks.shipping.dto.PaymentEventDTO;
import com.globalbooks.shipping.dto.ShipmentRequestDTO;
import com.globalbooks.shipping.model.ShippingMethod;
import com.globalbooks.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShippingMessageConsumer {

    private final ShippingService shippingService;

    @RabbitListener(queues = "payment.status.queue")
    public void handlePaymentStatusEvent(PaymentEventDTO event) {
        log.info("Received payment event: {} for order: {}", event.getEventType(), event.getOrderId());

        try {
            if ("PAYMENT_COMPLETED".equals(event.getEventType())) {
                // Create shipment when payment is completed
                ShipmentRequestDTO shipmentRequest = new ShipmentRequestDTO();
                shipmentRequest.setOrderId(event.getOrderId());
                shipmentRequest.setCustomerId(event.getCustomerId());
                shipmentRequest.setShippingMethod(ShippingMethod.STANDARD);

                // In production, get address from order service or event
                ShipmentRequestDTO.AddressDTO address = new ShipmentRequestDTO.AddressDTO();
                address.setStreet("123 Main St");
                address.setCity("New York");
                address.setState("NY");
                address.setZipCode("10001");
                address.setCountry("USA");
                shipmentRequest.setShippingAddress(address);

                shippingService.createShipment(shipmentRequest);
                log.info("Shipment created for paid order: {}", event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Error processing payment event for order: " + event.getOrderId(), e);
            throw e; // Will trigger retry mechanism
        }
    }

    @RabbitListener(queues = RabbitMQConfig.SHIPPING_REQUEST_QUEUE)
    public void handleShippingRequest(ShipmentRequestDTO request) {
        log.info("Received shipping request for order: {}", request.getOrderId());

        try {
            shippingService.createShipment(request);
        } catch (Exception e) {
            log.error("Error processing shipping request for order: " + request.getOrderId(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.SHIPPING_DLQ)
    public void handleDeadLetterMessage(Object message) {
        log.error("Received dead letter message: {}", message);
        // In production: alert, store for manual processing, etc.
    }
}