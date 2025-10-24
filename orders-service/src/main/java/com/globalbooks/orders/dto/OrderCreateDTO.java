package com.globalbooks.orders.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class OrderCreateDTO {

    @NotNull(message = "Customer ID is required")
    private String customerId;

    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemDTO> items;

    @NotNull(message = "Shipping address is required")
    private ShippingAddressDTO shippingAddress;

    @NotNull(message = "Payment method is required")
    private String paymentMethod;

    private Map<String, String> paymentDetails;
}