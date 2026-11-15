
package com.distributor.retailer.repository;

import com.distributor.retailer.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findAllByOrderByCreatedAtDesc();

    List<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(
            Long productId);
}
