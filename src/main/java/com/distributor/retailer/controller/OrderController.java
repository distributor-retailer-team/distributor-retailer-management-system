
package com.distributor.retailer.controller;

import com.distributor.retailer.dto.CreateOrderRequest;
import com.distributor.retailer.dto.OrderResponse;
import com.distributor.retailer.entity.Order;
import com.distributor.retailer.service.OrderService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/retailer/{retailerId}")
    public List<OrderResponse> getOrdersByRetailer(
            @PathVariable Long retailerId) {
        return orderService.getOrdersByRetailer(retailerId);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        return orderService.updateOrderStatus(id, status);
    }
}
