package com.globalbooks.orders.dto;

import jakarta.validation.constraints.NotNull;
import com.globalbooks.orders.model.OrderStatus;
import lombok.Data;

@Data
public class OrderStatusUpdateDTO {

    @NotNull(message = "Status is required")
    private OrderStatus status;
}