package com.globalbooks.orders.controller;

import com.globalbooks.orders.dto.*;
import com.globalbooks.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create new order")
    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_USER', 'SCOPE_ROLE_ADMIN')")
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderCreateDTO orderDTO,
            Authentication authentication) {
        log.info("REST request to create order by user: {}", authentication.getName());

        // Use authenticated username, not the customerId from request body
        OrderResponseDTO createdOrder = orderService.createOrder(orderDTO, authentication.getName());
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_USER', 'SCOPE_ROLE_ADMIN')")
    public ResponseEntity<OrderResponseDTO> getOrder(
            @PathVariable String orderId,
            Authentication authentication) {
        log.info("REST request to get order: {} by user: {}", orderId, authentication.getName());

        // Users can only see their own orders, admins can see all
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SCOPE_ROLE_ADMIN"))) {
            // Verify user owns this order (implement in service)
            orderService.verifyOrderOwnership(orderId, authentication.getName());
        }

        OrderResponseDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @Operation(summary = "Get all orders or filter by customer")
    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_USER', 'SCOPE_ROLE_ADMIN')")
    public ResponseEntity<List<OrderResponseDTO>> getOrders(
            @RequestParam(required = false) String customerId,
            Authentication authentication) {
        log.info("REST request to get orders by user: {}", authentication.getName());

        List<OrderResponseDTO> orders;

        // Regular users can only see their own orders
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SCOPE_ROLE_ADMIN"))) {
            customerId = authentication.getName(); // Force to current user
            orders = orderService.getOrdersByCustomer(customerId);
        } else {
            // Admins can see all or filter by customer
            if (customerId != null) {
                orders = orderService.getOrdersByCustomer(customerId);
            } else {
                orders = orderService.getAllOrders();
            }
        }

        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody OrderStatusUpdateDTO statusUpdate,
            Authentication authentication) {
        log.info("REST request to update order {} status by admin: {}",
                orderId, authentication.getName());
        OrderResponseDTO updatedOrder = orderService.updateOrderStatus(orderId, statusUpdate);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel/Delete an order")
    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_USER', 'SCOPE_ROLE_ADMIN')")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable String orderId,
            Authentication authentication) {
        log.info("REST request to delete order: {} by user: {}",
                orderId, authentication.getName());

        // Users can only delete their own orders
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SCOPE_ROLE_ADMIN"))) {
            orderService.verifyOrderOwnership(orderId, authentication.getName());
        }

        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}