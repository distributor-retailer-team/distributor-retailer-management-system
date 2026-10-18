
package com.distributor.retailer.service;

import com.distributor.retailer.dto.CreateOrderRequest;
import com.distributor.retailer.dto.OrderResponse;
import com.distributor.retailer.entity.Order;
import com.distributor.retailer.entity.OrderItem;
import com.distributor.retailer.entity.Product;
import com.distributor.retailer.entity.Retailer;
import com.distributor.retailer.repository.OrderItemRepository;
import com.distributor.retailer.repository.OrderRepository;
import com.distributor.retailer.repository.ProductRepository;
import com.distributor.retailer.repository.RetailerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final RetailerRepository retailerRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            RetailerRepository retailerRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.retailerRepository = retailerRepository;
    }

    private OrderResponse toResponse(Order order) {

        List<OrderItem> orderItems =
                orderItemRepository.findByOrderId(order.getId());

        List<OrderResponse.OrderItemResponse> itemResponses =
                orderItems.stream()
                        .map(item -> new OrderResponse.OrderItemResponse(
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getProduct().getSku(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getSubtotal()
                        ))
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getRetailer().getId(),
                order.getRetailer().getName(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getOrderDate(),
                itemResponses
        );
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        Retailer retailer = retailerRepository.findById(request.getRetailerId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Retailer not found"));

        Order order = new Order();
        order.setRetailer(retailer);
        order.setStatus(Order.OrderStatus.PENDING);

        order = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        // Combine repeated product IDs.
        Map<Long, Integer> quantities = new HashMap<>();

        for (CreateOrderRequest.OrderItemRequest itemRequest
                : request.getItems()) {

            Integer existingQuantity =
                    quantities.get(itemRequest.getProductId());

            int newQuantity = Math.addExact(
                    existingQuantity == null ? 0 : existingQuantity,
                    itemRequest.getQuantity()
            );

            quantities.put(itemRequest.getProductId(), newQuantity);
        }

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {

            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Product not found: " + entry.getKey()));

            int quantity = entry.getValue();

            if (product.getStockQuantity() < quantity) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product: "
                                + product.getName());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(quantity));

            product.setStockQuantity(
                    product.getStockQuantity() - quantity);
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(subtotal);

            items.add(orderItem);
            total = total.add(subtotal);
        }

        orderItemRepository.saveAll(items);

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found: " + id));

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByRetailer(Long retailerId) {

        if (!retailerRepository.existsById(retailerId)) {
            throw new IllegalArgumentException("Retailer not found");
        }

        return orderRepository.findByRetailerId(retailerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(
            Long id, Order.OrderStatus newStatus) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found: " + id));

        if (order.getStatus() == Order.OrderStatus.CANCELLED
                || order.getStatus() == Order.OrderStatus.DELIVERED) {

            throw new IllegalArgumentException(
                    "Cannot change status of a delivered or cancelled order");
        }

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Order status is required");
        }

        if (newStatus == Order.OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Order cancellation requires a separate stock-restoration workflow");
        }

        order.setStatus(newStatus);

        Order savedOrder = orderRepository.save(order);

        return toResponse(savedOrder);
    }
}
