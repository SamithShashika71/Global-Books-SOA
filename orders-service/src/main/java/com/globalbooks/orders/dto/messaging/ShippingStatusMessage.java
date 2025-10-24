package com.globalbooks.orders.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingStatusMessage {
    private String shipmentId;
    private String orderId;
    private String customerId;
    private String status; // READY_TO_SHIP, SHIPPED, IN_TRANSIT, DELIVERED
    private String trackingNumber;
    private String carrier;
    private String estimatedDelivery;
    private LocalDateTime timestamp;
}