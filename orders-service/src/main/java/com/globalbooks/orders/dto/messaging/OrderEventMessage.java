package com.globalbooks.orders.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventMessage {
    private String eventId;
    private String eventType; // ORDER_CREATED, ORDER_UPDATED, ORDER_CANCELLED
    private String orderId;
    private String customerId;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemDto> items;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}