
package com.distributor.retailer.repository;

import com.distributor.retailer.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    List<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
            String name, String sku);
}
