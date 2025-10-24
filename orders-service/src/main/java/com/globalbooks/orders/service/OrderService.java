package com.globalbooks.orders.service;

import com.globalbooks.orders.dto.*;
import com.globalbooks.orders.dto.messaging.OrderEventMessage;
import com.globalbooks.orders.dto.messaging.PaymentRequestMessage;
import com.globalbooks.orders.messaging.OrderMessageProducer;
import com.globalbooks.orders.exception.OrderNotFoundException;
import com.globalbooks.orders.exception.InvalidOrderException;
import com.globalbooks.orders.model.Order;
import com.globalbooks.orders.model.OrderItem;
import com.globalbooks.orders.model.OrderStatus;
import com.globalbooks.orders.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMessageProducer messageProducer;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderResponseDTO createOrder(OrderCreateDTO orderDTO, String authenticatedUsername) {
        log.info("Creating new order for authenticated customer: {}", authenticatedUsername);

        // Generate order ID
        String orderId = generateOrderId();

        // Create order entity
        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomerId(authenticatedUsername); // Use authenticated username for security
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(orderDTO.getPaymentMethod());

        // Convert shipping address to JSON
        try {
            String addressJson = objectMapper.writeValueAsString(orderDTO.getShippingAddress());
            order.setShippingAddress(addressJson);
        } catch (Exception e) {
            throw new InvalidOrderException("Invalid shipping address format");
        }

        // Create order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDTO itemDTO : orderDTO.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(itemDTO.getProductId());
            item.setQuantity(itemDTO.getQuantity());

            // In real scenario, fetch price from CatalogService
            BigDecimal price = itemDTO.getPrice() != null ? itemDTO.getPrice() : new BigDecimal("29.99");
            item.setUnitPrice(price);

            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            item.setSubtotal(subtotal);

            totalAmount = totalAmount.add(subtotal);
            order.getItems().add(item);
        }

        order.setTotalAmount(totalAmount);

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {} for customer: {}", savedOrder.getOrderId(), authenticatedUsername);

        // Send payment request to PaymentsService via RabbitMQ
        PaymentRequestMessage paymentRequest = PaymentRequestMessage.builder()
                .orderId(savedOrder.getOrderId())
                .customerId(savedOrder.getCustomerId())
                .amount(savedOrder.getTotalAmount())
                .currency("USD")
                .paymentMethod(orderDTO.getPaymentMethod())
                .paymentDetails(convertToPaymentDetails(orderDTO))
                .build();

        messageProducer.sendPaymentRequest(paymentRequest);

        // Publish order created event
        OrderEventMessage orderEvent = OrderEventMessage.builder()
                .orderId(savedOrder.getOrderId())
                .customerId(savedOrder.getCustomerId())
                .totalAmount(savedOrder.getTotalAmount())
                .status(savedOrder.getStatus().toString())
                .items(convertToItemDtos(savedOrder.getItems()))
                .build();

        messageProducer.publishOrderCreatedEvent(orderEvent);

        // Update order status to payment pending
        savedOrder.setStatus(OrderStatus.PAYMENT_PENDING);
        orderRepository.save(savedOrder);

        return convertToResponseDTO(savedOrder);
    }

    public void initiatePaymentForOrder(String orderId, PaymentRequestMessage paymentRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        paymentRequest.setOrderId(orderId);
        paymentRequest.setCustomerId(order.getCustomerId());
        paymentRequest.setAmount(order.getTotalAmount());

        messageProducer.sendPaymentRequest(paymentRequest);

        order.setStatus(OrderStatus.PAYMENT_PENDING);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(String orderId) {
        log.info("Fetching order: {}", orderId);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        return convertToResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByCustomer(String customerId) {
        log.info("Fetching orders for customer: {}", customerId);

        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orders.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        log.info("Fetching all orders");

        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(String orderId, OrderStatusUpdateDTO statusUpdate) {
        log.info("Updating order {} status to {}", orderId, statusUpdate.getStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), statusUpdate.getStatus());

        order.setStatus(statusUpdate.getStatus());

        // Publish status update event
        messageProducer.publishOrderStatusUpdate(orderId, statusUpdate.getStatus().toString());

        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} status updated successfully", orderId);
        return convertToResponseDTO(updatedOrder);
    }

    @Transactional
    public void deleteOrder(String orderId) {
        log.info("Deleting order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        // Only allow deletion of PENDING or CANCELLED orders
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Cannot delete order in status: " + order.getStatus());
        }

        orderRepository.delete(order);
        log.info("Order {} deleted successfully", orderId);
    }

    /**
     * Verifies that the specified user owns the given order
     * Throws AccessDeniedException if the user doesn't own the order
     */
    @Transactional(readOnly = true)
    public void verifyOrderOwnership(String orderId, String userId) {
        log.info("Verifying ownership of order {} for user {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (!order.getCustomerId().equals(userId)) {
            log.warn("Access denied: User {} attempted to access order {} owned by {}",
                    userId, orderId, order.getCustomerId());
            throw new AccessDeniedException("Access denied: You can only access your own orders");
        }

        log.info("Order ownership verified successfully for user {}", userId);
    }

    // Helper methods
    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid transitions including new statuses
        boolean validTransition = switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.PAYMENT_PENDING ||
                    newStatus == OrderStatus.CANCELLED;
            case PAYMENT_PENDING -> newStatus == OrderStatus.PAYMENT_COMPLETED ||
                    newStatus == OrderStatus.PAYMENT_FAILED ||
                    newStatus == OrderStatus.CANCELLED;
            case PAYMENT_COMPLETED -> newStatus == OrderStatus.PROCESSING ||
                    newStatus == OrderStatus.READY_TO_SHIP ||
                    newStatus == OrderStatus.REFUNDED;
            case PAYMENT_FAILED -> newStatus == OrderStatus.PAYMENT_PENDING ||
                    newStatus == OrderStatus.CANCELLED;
            case PROCESSING -> newStatus == OrderStatus.READY_TO_SHIP ||
                    newStatus == OrderStatus.CANCELLED;
            case READY_TO_SHIP -> newStatus == OrderStatus.SHIPPED;
            case SHIPPED -> newStatus == OrderStatus.IN_TRANSIT ||
                    newStatus == OrderStatus.DELIVERED;
            case IN_TRANSIT -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED -> newStatus == OrderStatus.REFUNDED;
            case CANCELLED, REFUNDED -> false; // Terminal states
            // Handle old statuses for backward compatibility
            case CONFIRMED -> newStatus == OrderStatus.PROCESSING ||
                    newStatus == OrderStatus.SHIPPED ||
                    newStatus == OrderStatus.CANCELLED;
        };

        if (!validTransition) {
            throw new InvalidOrderException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }
    }

    private PaymentRequestMessage.PaymentDetails convertToPaymentDetails(OrderCreateDTO dto) {

        if (dto.getPaymentDetails() == null) {
            return null;
        }

        // Extract payment details from the order DTO if available
        PaymentRequestMessage.PaymentDetails.PaymentDetailsBuilder builder =
                PaymentRequestMessage.PaymentDetails.builder();

        // Map payment details from DTO
        builder.cardNumber(dto.getPaymentDetails().get("cardNumber"))
                .cardHolderName(dto.getPaymentDetails().get("cardHolderName"))
                .expiryMonth(dto.getPaymentDetails().get("expiryMonth"))
                .expiryYear(dto.getPaymentDetails().get("expiryYear"))
                .cvv(dto.getPaymentDetails().get("cvv"));

        // Add email from shipping address if available
        if (dto.getShippingAddress() != null && dto.getShippingAddress().getEmail() != null) {
            builder.email(dto.getShippingAddress().getEmail());
        }

        return builder.build();
    }

    private List<OrderEventMessage.OrderItemDto> convertToItemDtos(List<OrderItem> items) {
        return items.stream()
                .map(item -> OrderEventMessage.OrderItemDto.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName() != null ?
                                item.getProductName() : "Product " + item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());
    }

    private OrderResponseDTO convertToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setCustomerId(order.getCustomerId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Add RabbitMQ-related fields if they exist
        dto.setPaymentId(order.getPaymentId());
        dto.setTransactionId(order.getTransactionId());
        dto.setShipmentId(order.getShipmentId());
        dto.setTrackingNumber(order.getTrackingNumber());

        // Convert shipping address from JSON
        try {
            if (order.getShippingAddress() != null) {
                ShippingAddressDTO address = objectMapper.readValue(
                        order.getShippingAddress(),
                        ShippingAddressDTO.class
                );
                dto.setShippingAddress(address);
            }
        } catch (Exception e) {
            log.error("Error parsing shipping address", e);
        }

        // Convert items
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> {
                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setProductId(item.getProductId());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setPrice(item.getUnitPrice());
                    return itemDTO;
                })
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }
}