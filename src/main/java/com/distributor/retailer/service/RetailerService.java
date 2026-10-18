
package com.distributor.retailer.service;

import com.distributor.retailer.entity.Retailer;
import com.distributor.retailer.repository.RetailerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetailerService {

    private final RetailerRepository retailerRepository;

    public RetailerService(RetailerRepository retailerRepository) {
        this.retailerRepository = retailerRepository;
    }

    public Retailer createRetailer(Retailer retailer) {
        if (retailerRepository.findByPhone(retailer.getPhone()).isPresent()) {
            throw new IllegalArgumentException(
                    "A retailer with this phone number already exists");
        }

        return retailerRepository.save(retailer);
    }

    public List<Retailer> getAllRetailers() {
        return retailerRepository.findAll();
    }

    public Retailer getRetailerById(Long id) {
        return retailerRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Retailer not found with id: " + id));
    }

    public List<Retailer> searchRetailersByName(String name) {
        if (name == null || name.isBlank()) {
            return getAllRetailers();
        }

        return retailerRepository.findByNameContainingIgnoreCase(name.trim());
    }

    public Retailer updateRetailer(Long id, Retailer updatedRetailer) {
        Retailer existingRetailer = getRetailerById(id);

        retailerRepository.findByPhone(updatedRetailer.getPhone())
                .filter(retailer -> !retailer.getId().equals(id))
                .ifPresent(retailer -> {
                    throw new IllegalArgumentException(
                            "This phone number is already used by another retailer");
                });

        existingRetailer.setName(updatedRetailer.getName());
        existingRetailer.setOwnerName(updatedRetailer.getOwnerName());
        existingRetailer.setPhone(updatedRetailer.getPhone());
        existingRetailer.setEmail(updatedRetailer.getEmail());
        existingRetailer.setAddress(updatedRetailer.getAddress());

        return retailerRepository.save(existingRetailer);
    }

    public void deleteRetailer(Long id) {
        Retailer retailer = getRetailerById(id);
        retailerRepository.delete(retailer);
    }
}
