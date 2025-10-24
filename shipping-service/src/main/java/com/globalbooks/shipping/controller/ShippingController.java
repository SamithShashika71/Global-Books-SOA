package com.globalbooks.shipping.controller;

import com.globalbooks.shipping.dto.ShipmentRequestDTO;
import com.globalbooks.shipping.dto.ShipmentResponseDTO;
import com.globalbooks.shipping.dto.ShipmentStatusUpdateDTO;
import com.globalbooks.shipping.model.ShipmentStatus;
import com.globalbooks.shipping.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
@Tag(name = "Shipping Management", description = "APIs for shipment processing")
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping
    @Operation(summary = "Create a new shipment")
    public ResponseEntity<ShipmentResponseDTO> createShipment(@Valid @RequestBody ShipmentRequestDTO request) {
        ShipmentResponseDTO response = shippingService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shipmentId}")
    @Operation(summary = "Get shipment by ID")
    public ResponseEntity<ShipmentResponseDTO> getShipment(@PathVariable String shipmentId) {
        ShipmentResponseDTO shipment = shippingService.getShipmentById(shipmentId);
        return ResponseEntity.ok(shipment);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get shipment by order ID")
    public ResponseEntity<ShipmentResponseDTO> getShipmentByOrder(@PathVariable String orderId) {
        ShipmentResponseDTO shipment = shippingService.getShipmentByOrderId(orderId);
        return ResponseEntity.ok(shipment);
    }

    @GetMapping("/track/{trackingNumber}")
    @Operation(summary = "Track shipment by tracking number")
    public ResponseEntity<ShipmentResponseDTO> trackShipment(@PathVariable String trackingNumber) {
        ShipmentResponseDTO shipment = shippingService.getShipmentByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(shipment);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all shipments for a customer")
    public ResponseEntity<List<ShipmentResponseDTO>> getCustomerShipments(@PathVariable String customerId) {
        List<ShipmentResponseDTO> shipments = shippingService.getShipmentsByCustomer(customerId);
        return ResponseEntity.ok(shipments);
    }

    @PutMapping("/{shipmentId}/status")
    @Operation(summary = "Update shipment status")
    public ResponseEntity<ShipmentResponseDTO> updateStatus(
            @PathVariable String shipmentId,
            @RequestBody Map<String, String> request) {
        ShipmentStatus status = ShipmentStatus.valueOf(request.get("status"));
        ShipmentResponseDTO response = shippingService.updateShipmentStatus(shipmentId, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{shipmentId}/deliver")
    @Operation(summary = "Mark shipment as delivered")
    public ResponseEntity<ShipmentResponseDTO> markDelivered(@PathVariable String shipmentId) {
        ShipmentResponseDTO response = shippingService.markAsDelivered(shipmentId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{shipmentId}")
    @Operation(summary = "Cancel a shipment")
    public ResponseEntity<Void> cancelShipment(@PathVariable String shipmentId) {
        shippingService.cancelShipment(shipmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "shipping-service",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}