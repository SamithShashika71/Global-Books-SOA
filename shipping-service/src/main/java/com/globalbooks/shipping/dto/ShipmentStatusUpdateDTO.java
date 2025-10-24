package com.globalbooks.shipping.dto;

import com.globalbooks.shipping.model.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShipmentStatusUpdateDTO {

    @NotNull(message = "Status is required")
    private ShipmentStatus status;

    private String notes;

    private String location;

    private String updatedBy;
}