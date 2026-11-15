
package com.distributor.retailer.service;

import com.distributor.retailer.entity.InventoryTransaction;
import com.distributor.retailer.entity.Product;
import com.distributor.retailer.repository.InventoryTransactionRepository;
import com.distributor.retailer.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;

    public InventoryService(
            ProductRepository productRepository,
            InventoryTransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public InventoryTransaction adjustStock(
            Long productId,
            Integer quantity,
            String transactionType,
            String note) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero.");
        }

        if (transactionType == null ||
                (!transactionType.equalsIgnoreCase("STOCK_IN")
                        && !transactionType.equalsIgnoreCase("STOCK_OUT"))) {
            throw new IllegalArgumentException(
                    "Transaction type must be STOCK_IN or STOCK_OUT.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        int previousStock = product.getStockQuantity() == null
                ? 0
                : product.getStockQuantity();

        int change = transactionType.equalsIgnoreCase("STOCK_IN")
                ? quantity
                : -quantity;

        int newStock = previousStock + change;

        if (newStock < 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock available.");
        }

        product.setStockQuantity(newStock);
        productRepository.save(product);

        InventoryTransaction transaction = new InventoryTransaction();

        transaction.setProductId(product.getId());
        transaction.setProductName(product.getName());
        transaction.setSku(product.getSku());
        transaction.setPreviousStock(previousStock);
        transaction.setQuantityChanged(change);
        transaction.setNewStock(newStock);
        transaction.setTransactionType(
                transactionType.toUpperCase());
        transaction.setNote(note);

        return transactionRepository.save(transaction);
    }

    public List<InventoryTransaction> getAllHistory() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<InventoryTransaction> getProductHistory(Long productId) {
        return transactionRepository
                .findByProductIdOrderByCreatedAtDesc(productId);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findAll()
                .stream()
                .filter(product -> product.getStockQuantity() != null
                        && product.getMinimumStockLevel() != null
                        && product.getStockQuantity() <= product.getMinimumStockLevel())
                .toList();
    }
}
