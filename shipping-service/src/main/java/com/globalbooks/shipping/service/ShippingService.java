package com.globalbooks.shipping.service;

import com.globalbooks.shipping.dto.ShipmentRequestDTO;
import com.globalbooks.shipping.dto.ShipmentResponseDTO;
import com.globalbooks.shipping.dto.ShipmentStatusUpdateDTO;
import com.globalbooks.shipping.model.ShipmentStatus;

import java.util.List;

public interface ShippingService {
    ShipmentResponseDTO createShipment(ShipmentRequestDTO request);
    ShipmentResponseDTO getShipmentById(String shipmentId);
    ShipmentResponseDTO getShipmentByOrderId(String orderId);
    ShipmentResponseDTO getShipmentByTrackingNumber(String trackingNumber);
    List<ShipmentResponseDTO> getShipmentsByCustomer(String customerId);
    ShipmentResponseDTO updateShipmentStatus(String shipmentId, ShipmentStatus status);
    void cancelShipment(String shipmentId);
    ShipmentResponseDTO markAsDelivered(String shipmentId);
}