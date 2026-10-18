
package com.distributor.retailer.dto;

import com.distributor.retailer.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long id;
    private Long retailerId;
    private String retailerName;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private List<OrderItemResponse> items;

    public OrderResponse(
            Long id,
            Long retailerId,
            String retailerName,
            Order.OrderStatus status,
            BigDecimal totalAmount,
            LocalDateTime orderDate,
            List<OrderItemResponse> items) {
        this.id = id;
        this.retailerId = retailerId;
        this.retailerName = retailerName;
        this.status = status;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.items = items;
    }

    public Long getId() { return id; }
    public Long getRetailerId() { return retailerId; }
    public String getRetailerName() { return retailerName; }
    public Order.OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public List<OrderItemResponse> getItems() { return items; }

    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private String sku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        public OrderItemResponse(
                Long productId,
                String productName,
                String sku,
                Integer quantity,
                BigDecimal unitPrice,
                BigDecimal subtotal) {
            this.productId = productId;
            this.productName = productName;
            this.sku = sku;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.subtotal = subtotal;
        }

        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getSku() { return sku; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getSubtotal() { return subtotal; }
    }
}

