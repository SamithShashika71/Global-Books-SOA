package com.globalbooks.orders.dto;

import lombok.Data;
import com.globalbooks.orders.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private String orderId;
    private String customerId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private ShippingAddressDTO shippingAddress;
    private String paymentMethod;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Payment and shipping related fields
    private String paymentId;
    private String transactionId;
    private String shipmentId;
    private String trackingNumber;
}