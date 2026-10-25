
package com.distributor.retailer.service;

import com.distributor.retailer.entity.Product;
import com.distributor.retailer.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new IllegalArgumentException(
                    "Product with SKU already exists: " + product.getSku());
        }

        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllProducts();
        }

        String searchTerm = keyword.trim();

        return productRepository
                .findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
                        searchTerm, searchTerm);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found with id: " + id));
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = getProductById(id);

        if (productRepository.existsBySkuAndIdNot(
                updatedProduct.getSku(), id)) {
            throw new IllegalArgumentException(
                    "Another product already uses SKU: "
                            + updatedProduct.getSku());
        }

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setSku(updatedProduct.getSku());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
        existingProduct.setMinimumStockLevel(
                updatedProduct.getMinimumStockLevel());

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}
