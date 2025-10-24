package com.globalbooks.orders.model;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    PROCESSING,
    READY_TO_SHIP,
    IN_TRANSIT,
    REFUNDED
}