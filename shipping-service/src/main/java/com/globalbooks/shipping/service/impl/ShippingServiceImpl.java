package com.globalbooks.shipping.service.impl;

import com.globalbooks.shipping.dto.ShipmentRequestDTO;
import com.globalbooks.shipping.dto.ShipmentResponseDTO;
import com.globalbooks.shipping.dto.ShipmentStatusUpdateDTO;
import com.globalbooks.shipping.exception.ShippingException;
import com.globalbooks.shipping.messaging.ShippingEventPublisher;
import com.globalbooks.shipping.model.*;
import com.globalbooks.shipping.repository.ShipmentRepository;
import com.globalbooks.shipping.service.ShippingService;
import com.globalbooks.shipping.util.ShippingCostCalculator;
import com.globalbooks.shipping.util.TrackingNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingServiceImpl implements ShippingService {

    private final ShipmentRepository shipmentRepository;
    private final ShippingEventPublisher eventPublisher;
    private final TrackingNumberGenerator trackingNumberGenerator;
    private final ShippingCostCalculator costCalculator;

    @Override
    @Transactional
    public ShipmentResponseDTO createShipment(ShipmentRequestDTO request) {
        log.info("Creating shipment for order: {}", request.getOrderId());

        // Check if shipment already exists
        if (shipmentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new ShippingException("Shipment already exists for order: " + request.getOrderId());
        }

        Shipment shipment = new Shipment();
        shipment.setOrderId(request.getOrderId());
        shipment.setCustomerId(request.getCustomerId());
        shipment.setTrackingNumber(trackingNumberGenerator.generate());
        shipment.setStatus(ShipmentStatus.READY_TO_SHIP);
        shipment.setShippingMethod(request.getShippingMethod());
        shipment.setCarrier("GlobalBooks Express");

        // Set address
        Address address = new Address();
        address.setStreet(request.getShippingAddress().getStreet());
        address.setCity(request.getShippingAddress().getCity());
        address.setState(request.getShippingAddress().getState());
        address.setZipCode(request.getShippingAddress().getZipCode());
        address.setCountry(request.getShippingAddress().getCountry());
        address.setPhone(request.getShippingAddress().getPhone());
        address.setEmail(request.getShippingAddress().getEmail());
        shipment.setShippingAddress(address);

        // Calculate shipping cost and estimated delivery
        BigDecimal weight = request.getWeight() != null ? request.getWeight() : new BigDecimal("1.0");
        shipment.setWeight(weight);
        shipment.setShippingCost(costCalculator.calculate(weight, request.getShippingMethod()));
        shipment.setEstimatedDelivery(calculateEstimatedDelivery(request.getShippingMethod()));

        shipment = shipmentRepository.save(shipment);

        // Publish shipment created event
        eventPublisher.publishShipmentStatusUpdate(shipment);

        return buildResponseDTO(shipment);
    }

    @Override
    public ShipmentResponseDTO getShipmentById(String shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShippingException("Shipment not found: " + shipmentId));
        return buildResponseDTO(shipment);
    }

    @Override
    public ShipmentResponseDTO getShipmentByOrderId(String orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ShippingException("Shipment not found for order: " + orderId));
        return buildResponseDTO(shipment);
    }

    @Override
    public ShipmentResponseDTO getShipmentByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShippingException("Shipment not found with tracking: " + trackingNumber));
        return buildResponseDTO(shipment);
    }

    @Override
    public List<ShipmentResponseDTO> getShipmentsByCustomer(String customerId) {
        return shipmentRepository.findByCustomerId(customerId)
                .stream()
                .map(this::buildResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShipmentResponseDTO updateShipmentStatus(String shipmentId, ShipmentStatus status) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShippingException("Shipment not found: " + shipmentId));

        shipment.setStatus(status);

        if (status == ShipmentStatus.IN_TRANSIT) {
            shipment.setShippedAt(LocalDateTime.now());
        } else if (status == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(LocalDateTime.now());
            shipment.setActualDelivery(LocalDateTime.now());
        }

        shipment = shipmentRepository.save(shipment);

        // Publish status update event
        eventPublisher.publishShipmentStatusUpdate(shipment);

        return buildResponseDTO(shipment);
    }

    @Override
    @Transactional
    public void cancelShipment(String shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShippingException("Shipment not found: " + shipmentId));

        if (shipment.getStatus() != ShipmentStatus.PENDING &&
                shipment.getStatus() != ShipmentStatus.READY_TO_SHIP) {
            throw new ShippingException("Cannot cancel shipment in status: " + shipment.getStatus());
        }

        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipmentRepository.save(shipment);

        eventPublisher.publishShipmentStatusUpdate(shipment);
    }

    @Override
    @Transactional
    public ShipmentResponseDTO markAsDelivered(String shipmentId) {
        return updateShipmentStatus(shipmentId, ShipmentStatus.DELIVERED);
    }

    private LocalDateTime calculateEstimatedDelivery(ShippingMethod method) {
        return switch (method) {
            case SAME_DAY -> LocalDateTime.now().plusHours(8);
            case OVERNIGHT -> LocalDateTime.now().plusDays(1);
            case TWO_DAY -> LocalDateTime.now().plusDays(2);
            case EXPRESS -> LocalDateTime.now().plusDays(3);
            case STANDARD -> LocalDateTime.now().plusDays(5);
            case ECONOMY -> LocalDateTime.now().plusDays(7);
        };
    }

    private ShipmentResponseDTO buildResponseDTO(Shipment shipment) {
        return ShipmentResponseDTO.builder()
                .shipmentId(shipment.getId())
                .orderId(shipment.getOrderId())
                .trackingNumber(shipment.getTrackingNumber())
                .status(shipment.getStatus())
                .shippingMethod(shipment.getShippingMethod())
                .carrier(shipment.getCarrier())
                .shippingCost(shipment.getShippingCost())
                .estimatedDelivery(shipment.getEstimatedDelivery())
                .createdAt(shipment.getCreatedAt())
                .build();
    }
}