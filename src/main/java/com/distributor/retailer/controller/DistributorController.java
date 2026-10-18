
package com.distributor.retailer.controller;

import com.distributor.retailer.entity.Distributor;
import com.distributor.retailer.service.DistributorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/distributors")
public class DistributorController {

    private final DistributorService distributorService;

    public DistributorController(DistributorService distributorService) {
        this.distributorService = distributorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Distributor createDistributor(
            @Valid @RequestBody Distributor distributor) {
        return distributorService.createDistributor(distributor);
    }

    @GetMapping
    public List<Distributor> getAllDistributors(
            @RequestParam(required = false) String search) {

        if (search != null && !search.isBlank()) {
            return distributorService.searchDistributors(search);
        }

        return distributorService.getAllDistributors();
    }

    @GetMapping("/{id}")
    public Distributor getDistributorById(@PathVariable Long id) {
        return distributorService.getDistributorById(id);
    }

    @PutMapping("/{id}")
    public Distributor updateDistributor(
            @PathVariable Long id,
            @Valid @RequestBody Distributor distributor) {
        return distributorService.updateDistributor(id, distributor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDistributor(@PathVariable Long id) {
        distributorService.deleteDistributor(id);
    }
}
