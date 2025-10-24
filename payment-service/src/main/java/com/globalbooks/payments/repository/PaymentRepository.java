package com.globalbooks.payments.repository;

import com.globalbooks.payments.model.Payment;
import com.globalbooks.payments.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findByCustomerId(String customerId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.status = ?1 AND p.createdAt BETWEEN ?2 AND ?3")
    List<Payment> findByStatusAndDateRange(PaymentStatus status,
                                           LocalDateTime startDate,
                                           LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < ?1")
    List<Payment> findStalePayments(LocalDateTime cutoffTime);
}