
package com.distributor.retailer.service;

import com.distributor.retailer.entity.Retailer;
import com.distributor.retailer.repository.RetailerRepository;
import com.distributor.retailer.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class RetailerService {

    private final RetailerRepository retailerRepository;
    private final OrderRepository orderRepository;

    public RetailerService(RetailerRepository retailerRepository,
                           OrderRepository orderRepository) {
        this.retailerRepository = retailerRepository;
        this.orderRepository = orderRepository;
    }

    // Create a new retailer
    public Retailer createRetailer(Retailer retailer) {

        if (retailerRepository.findByPhone(retailer.getPhone()).isPresent()) {
            throw new IllegalArgumentException(
                "A retailer with this phone number already exists"
            );
        }

        return retailerRepository.save(retailer);
    }

    // Get all retailers
    public List<Retailer> getAllRetailers() {
        return retailerRepository.findAll();
    }

    // Get retailer by ID
    public Retailer getRetailerById(Long id) {

        return retailerRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Retailer not found with id: " + id
            ));
    }

    // Search retailers by name
    public List<Retailer> searchRetailersByName(String name) {

        if (name == null || name.isBlank()) {
            return getAllRetailers();
        }

        return retailerRepository.findByNameContainingIgnoreCase(
            name.trim()
        );
    }

    // Update retailer details
    public Retailer updateRetailer(Long id, Retailer updatedRetailer) {

        Retailer existingRetailer = getRetailerById(id);

        retailerRepository.findByPhone(updatedRetailer.getPhone())
            .filter(retailer -> !retailer.getId().equals(id))
            .ifPresent(retailer -> {
                throw new IllegalArgumentException(
                    "This phone number is already used by another retailer"
                );
            });

        existingRetailer.setName(updatedRetailer.getName());
        existingRetailer.setOwnerName(updatedRetailer.getOwnerName());
        existingRetailer.setPhone(updatedRetailer.getPhone());
        existingRetailer.setEmail(updatedRetailer.getEmail());
        existingRetailer.setAddress(updatedRetailer.getAddress());

        return retailerRepository.save(existingRetailer);
    }

    // Delete retailer only if no orders are linked to it
    public void deleteRetailer(Long id) {

        Retailer retailer = getRetailerById(id);

        if (!orderRepository.findByRetailerId(id).isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot delete retailer because existing orders are linked to it"
            );
        }

        retailerRepository.delete(retailer);
    }
}
