package com.globalbooks.shipping.repository;

import com.globalbooks.shipping.model.Shipment;
import com.globalbooks.shipping.model.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String> {

    Optional<Shipment> findByOrderId(String orderId);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByCustomerId(String customerId);

    List<Shipment> findByStatus(ShipmentStatus status);

    @Query("SELECT s FROM Shipment s WHERE s.status = 'IN_TRANSIT' AND s.estimatedDelivery < ?1")
    List<Shipment> findDelayedShipments(LocalDateTime currentTime);
}