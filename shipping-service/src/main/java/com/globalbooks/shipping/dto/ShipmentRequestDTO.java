package com.globalbooks.shipping.dto;

import com.globalbooks.shipping.model.ShippingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShipmentRequestDTO {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Shipping method is required")
    private ShippingMethod shippingMethod;

    @NotNull(message = "Shipping address is required")
    private AddressDTO shippingAddress;

    private BigDecimal weight;

    private String notes;

    @Data
    public static class AddressDTO {
        @NotBlank(message = "Street is required")
        private String street;

        @NotBlank(message = "City is required")
        private String city;

        private String state;

        @NotBlank(message = "Zip code is required")
        private String zipCode;

        private String country = "USA";

        private String phone;

        private String email;
    }
}