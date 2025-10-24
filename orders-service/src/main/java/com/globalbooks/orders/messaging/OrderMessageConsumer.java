package com.globalbooks.orders.messaging;

import com.globalbooks.orders.config.RabbitMQConfig;
import com.globalbooks.orders.dto.messaging.PaymentResponseMessage;
import com.globalbooks.orders.dto.messaging.ShippingStatusMessage;
import com.globalbooks.orders.model.Order;
import com.globalbooks.orders.model.OrderStatus;
import com.globalbooks.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMessageConsumer {

    private final OrderRepository orderRepository;

    /**
     * Handle payment response from PaymentsService
     */
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_RESPONSE_QUEUE)
    @Transactional
    public void handlePaymentResponse(PaymentResponseMessage response) {
        log.info("Received payment response for order: {} with status: {}",
                response.getOrderId(), response.getStatus());

        try {
            Order order = orderRepository.findById(response.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + response.getOrderId()));

            // Update order status based on payment result
            switch (response.getStatus()) {
                case "COMPLETED":
                    order.setStatus(OrderStatus.PAYMENT_COMPLETED);
                    order.setPaymentId(response.getPaymentId());
                    order.setTransactionId(response.getTransactionId());
                    log.info("Payment completed for order: {}", response.getOrderId());
                    break;

                case "FAILED":
                    order.setStatus(OrderStatus.PAYMENT_FAILED);
                    order.setPaymentFailureReason(response.getMessage());
                    log.warn("Payment failed for order: {} - {}",
                            response.getOrderId(), response.getMessage());
                    break;

                case "PENDING":
                    order.setStatus(OrderStatus.PAYMENT_PENDING);
                    log.info("Payment pending for order: {}", response.getOrderId());
                    break;

                default:
                    log.warn("Unknown payment status: {} for order: {}",
                            response.getStatus(), response.getOrderId());
            }

            orderRepository.save(order);

        } catch (Exception e) {
            log.error("Error processing payment response for order: " + response.getOrderId(), e);
            throw e; // Will trigger retry or DLQ
        }
    }

    /**
     * Handle shipping status updates from ShippingService
     */
    @RabbitListener(queues = RabbitMQConfig.SHIPPING_STATUS_QUEUE)
    @Transactional
    public void handleShippingStatus(ShippingStatusMessage status) {
        log.info("Received shipping status for order: {} - {}",
                status.getOrderId(), status.getStatus());

        try {
            Order order = orderRepository.findById(status.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + status.getOrderId()));

            // Update order status based on shipping status
            switch (status.getStatus()) {
                case "READY_TO_SHIP":
                    order.setStatus(OrderStatus.READY_TO_SHIP);
                    break;

                case "SHIPPED":
                    order.setStatus(OrderStatus.SHIPPED);
                    order.setShipmentId(status.getShipmentId());
                    order.setTrackingNumber(status.getTrackingNumber());
                    order.setCarrier(status.getCarrier());
                    log.info("Order shipped: {} with tracking: {}",
                            status.getOrderId(), status.getTrackingNumber());
                    break;

                case "IN_TRANSIT":
                    order.setStatus(OrderStatus.IN_TRANSIT);
                    break;

                case "DELIVERED":
                    order.setStatus(OrderStatus.DELIVERED);
                    order.setDeliveredAt(status.getTimestamp());
                    log.info("Order delivered: {}", status.getOrderId());
                    break;

                default:
                    log.warn("Unknown shipping status: {} for order: {}",
                            status.getStatus(), status.getOrderId());
            }

            orderRepository.save(order);

        } catch (Exception e) {
            log.error("Error processing shipping status for order: " + status.getOrderId(), e);
            throw e;
        }
    }

    /**
     * Handle dead letter messages
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_DLQ)
    public void handleDeadLetterMessage(Object message) {
        log.error("Received dead letter message: {}", message);
        // In production, you might want to:
        // 1. Send alerts
        // 2. Store for manual processing
        // 3. Implement compensation logic
    }
}