
package com.distributor.retailer.service;

import com.distributor.retailer.entity.Distributor;
import com.distributor.retailer.repository.DistributorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistributorService {

    private final DistributorRepository distributorRepository;

    public DistributorService(DistributorRepository distributorRepository) {
        this.distributorRepository = distributorRepository;
    }

    public Distributor createDistributor(Distributor distributor) {
        if (distributorRepository.existsByEmailIgnoreCase(
                distributor.getEmail().trim())) {
            throw new IllegalArgumentException(
                    "Distributor with this email already exists");
        }

        distributor.setEmail(distributor.getEmail().trim());
        return distributorRepository.save(distributor);
    }

    public List<Distributor> getAllDistributors() {
        return distributorRepository.findAll();
    }

    public List<Distributor> searchDistributors(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllDistributors();
        }

        String searchTerm = keyword.trim();

        return distributorRepository
                .findByNameContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(
                        searchTerm, searchTerm);
    }

    public Distributor getDistributorById(Long id) {
        return distributorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Distributor not found with id: " + id));
    }

    public Distributor updateDistributor(
            Long id, Distributor updatedDistributor) {

        Distributor existingDistributor = getDistributorById(id);
        String email = updatedDistributor.getEmail().trim();

        if (distributorRepository.existsByEmailIgnoreCaseAndIdNot(
                email, id)) {
            throw new IllegalArgumentException(
                    "Another distributor already uses this email");
        }

        existingDistributor.setName(updatedDistributor.getName());
        existingDistributor.setCompanyName(
                updatedDistributor.getCompanyName());
        existingDistributor.setEmail(email);
        existingDistributor.setPhone(updatedDistributor.getPhone());
        existingDistributor.setAddress(updatedDistributor.getAddress());

        return distributorRepository.save(existingDistributor);
    }

    public void deleteDistributor(Long id) {
        Distributor distributor = getDistributorById(id);
        distributorRepository.delete(distributor);
    }
}
