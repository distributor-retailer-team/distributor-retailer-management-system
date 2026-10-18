
package com.distributor.retailer.repository;

import com.distributor.retailer.entity.Distributor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DistributorRepository
        extends JpaRepository<Distributor, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Optional<Distributor> findByEmailIgnoreCase(String email);

    List<Distributor> findByNameContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(
            String name, String companyName);
}
