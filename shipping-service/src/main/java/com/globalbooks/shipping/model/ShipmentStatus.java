package com.globalbooks.shipping.model;

public enum ShipmentStatus {
    PENDING,
    READY_TO_SHIP,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED,
    RETURNED,
    CANCELLED
}