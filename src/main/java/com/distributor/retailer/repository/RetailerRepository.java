
package com.distributor.retailer.repository;

import com.distributor.retailer.entity.Retailer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RetailerRepository extends JpaRepository<Retailer, Long> {

    Optional<Retailer> findByPhone(String phone);

    List<Retailer> findByNameContainingIgnoreCase(String name);
}
