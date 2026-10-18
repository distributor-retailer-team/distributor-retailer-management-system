package com.distributor.retailer.dto;

import java.math.BigDecimal;

public class OrderSummaryResponse {

    private long totalOrders;
    private long pendingOrders;
    private long confirmedOrders;
    private long processingOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private BigDecimal totalSales;

    public OrderSummaryResponse(
            long totalOrders,
            long pendingOrders,
            long confirmedOrders,
            long processingOrders,
            long shippedOrders,
            long deliveredOrders,
            long cancelledOrders,
            BigDecimal totalSales) {

        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.confirmedOrders = confirmedOrders;
        this.processingOrders = processingOrders;
        this.shippedOrders = shippedOrders;
        this.deliveredOrders = deliveredOrders;
        this.cancelledOrders = cancelledOrders;
        this.totalSales = totalSales;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public long getConfirmedOrders() {
        return confirmedOrders;
    }

    public long getProcessingOrders() {
        return processingOrders;
    }

    public long getShippedOrders() {
        return shippedOrders;
    }

    public long getDeliveredOrders() {
        return deliveredOrders;
    }

    public long getCancelledOrders() {
        return cancelledOrders;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }
}