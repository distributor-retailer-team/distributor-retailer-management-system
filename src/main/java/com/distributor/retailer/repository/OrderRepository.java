
package com.distributor.retailer.repository;

import com.distributor.retailer.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByRetailerId(Long retailerId);

    List<Order> findByStatus(Order.OrderStatus status);
}
