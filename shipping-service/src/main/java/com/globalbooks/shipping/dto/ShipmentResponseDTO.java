package com.globalbooks.shipping.dto;

import com.globalbooks.shipping.model.ShipmentStatus;
import com.globalbooks.shipping.model.ShippingMethod;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ShipmentResponseDTO {
    private String shipmentId;
    private String orderId;
    private String trackingNumber;
    private ShipmentStatus status;
    private ShippingMethod shippingMethod;
    private String carrier;
    private BigDecimal shippingCost;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime createdAt;
}