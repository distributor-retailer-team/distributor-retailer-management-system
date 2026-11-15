
package com.distributor.retailer.controller;

import com.distributor.retailer.entity.InventoryTransaction;
import com.distributor.retailer.entity.Product;
import com.distributor.retailer.service.InventoryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/adjust")
    public ResponseEntity<InventoryTransaction> adjustStock(
            @RequestBody AdjustmentRequest request) {

        InventoryTransaction result = inventoryService.adjustStock(
                request.getProductId(),
                request.getQuantity(),
                request.getTransactionType(),
                request.getNote());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<InventoryTransaction>> getAllHistory() {
        return ResponseEntity.ok(
                inventoryService.getAllHistory());
    }

    @GetMapping("/history/{productId}")
    public ResponseEntity<List<InventoryTransaction>> getProductHistory(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                inventoryService.getProductHistory(productId));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        return ResponseEntity.ok(
                inventoryService.getLowStockProducts());
    }

    public static class AdjustmentRequest {

        private Long productId;
        private Integer quantity;
        private String transactionType;
        private String note;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getTransactionType() {
            return transactionType;
        }

        public void setTransactionType(String transactionType) {
            this.transactionType = transactionType;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }
}
