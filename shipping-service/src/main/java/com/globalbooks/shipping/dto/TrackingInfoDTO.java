package com.globalbooks.shipping.dto;

import com.globalbooks.shipping.model.ShipmentStatus;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TrackingInfoDTO {
    private String trackingNumber;
    private String orderId;
    private ShipmentStatus currentStatus;
    private String carrier;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private List<TrackingEvent> trackingHistory;

    @Data
    @Builder
    public static class TrackingEvent {
        private LocalDateTime timestamp;
        private String status;
        private String location;
        private String description;
    }
}